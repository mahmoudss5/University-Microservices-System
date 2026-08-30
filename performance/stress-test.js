import http from "k6/http";
import { check, group, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";

// Local test credentials. Change these two values for the account under test.
const USER_EMAIL = "fady@gmail.com"; // Must match a user in the IAM database.
const USER_PASSWORD = "test1234";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.25);
const MAX_VUS = Number(__ENV.MAX_VUS || 400);

const endpointLatency = {
  profile: new Trend("endpoint_profile_duration", true),
  courses: new Trend("endpoint_courses_duration", true),
  departments: new Trend("endpoint_departments_duration", true),
  feedback: new Trend("endpoint_feedback_duration", true),
};
const endpointFailures = new Rate("endpoint_failures");
const completedJourneys = new Counter("completed_journeys");

export const options = {
  scenarios: {
    university_api_stress: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: __ENV.WARM_UP || "20s", target: Math.round(MAX_VUS * 0.10) },
        { duration: __ENV.NORMAL_LOAD || "40s", target: Math.round(MAX_VUS * 0.25) },
        { duration: __ENV.STRESS_LOAD || "60s", target: Math.round(MAX_VUS * 0.60) },
        { duration: __ENV.SPIKE_LOAD || "100s", target: MAX_VUS },
        { duration: __ENV.RECOVERY || "30s", target: Math.round(MAX_VUS * 0.10) },
        { duration: __ENV.COOL_DOWN || "10s", target: 0 },
      ],
      gracefulRampDown: "10s",
    },
  },
  thresholds: {
    http_req_failed: ["rate<0.02"],
    http_req_duration: ["p(95)<1000", "p(99)<2000"],
    checks: ["rate>0.98"],
    endpoint_failures: ["rate<0.02"],
  },
};

export function setup() {
  const response = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: USER_EMAIL, password: USER_PASSWORD }),
    { headers: { "Content-Type": "application/json" }, tags: { endpoint: "login" } },
  );

  const authenticated = check(response, {
    "login returned 200": (r) => r.status === 200,
    "login returned a token": (r) => Boolean(r.json("token")),
  });
  if (!authenticated) {
    throw new Error(`Login failed (${response.status}). Check USER_EMAIL, USER_PASSWORD, and BASE_URL.`);
  }
  return { token: response.json("token") };
}

function get(name, path, token, authenticated = true) {
  const response = http.get(`${BASE_URL}${path}`, {
    headers: authenticated ? { Authorization: `Bearer ${token}` } : {},
    tags: { endpoint: name },
  });
  endpointLatency[name].add(response.timings.duration);
  const successful = check(response, {
    [`${name}: status is 200`]: (r) => r.status === 200,
    [`${name}: response is JSON`]: (r) =>
      (r.headers["Content-Type"] || "").toLowerCase().includes("application/json"),
  });
  endpointFailures.add(!successful);
}

export default function (data) {
  group("authenticated user journey", () => {
    get("profile", "/api/users/me", data.token);
    get("courses", "/api/courses", data.token);
  });
  group("public catalogue", () => {
    get("departments", "/api/departments/all", data.token, false);
    get("feedback", "/api/feedbacks/recent", data.token, false);
  });
  completedJourneys.add(1);
  sleep(THINK_TIME_SECONDS);
}

function metricValues(data, name) {
  return data.metrics[name] ? data.metrics[name].values : {};
}

export function handleSummary(data) {
  const duration = metricValues(data, "http_req_duration");
  const requests = metricValues(data, "http_reqs");
  const failures = metricValues(data, "http_req_failed");
  const checks = metricValues(data, "checks");
  const vus = metricValues(data, "vus_max");
  const endpointNames = ["profile", "courses", "departments", "feedback"];

  const thresholds = Object.entries(data.metrics)
    .filter(([, metric]) => metric.thresholds)
    .flatMap(([metricName, metric]) =>
      Object.entries(metric.thresholds).map(([rule, result]) => ({
        metric: metricName,
        rule,
        passed: result.ok === true,
      })),
    );

  const report = {
    schemaVersion: 1,
    title: "University API Stress Test",
    generatedAt: new Date().toISOString(),
    configuration: {
      baseUrl: BASE_URL,
      scenario: "Ramping stress test",
      credentials: { email: USER_EMAIL, password: "••••••••" },
    },
    overview: {
      status: thresholds.every((threshold) => threshold.passed) ? "passed" : "failed",
      requests: requests.count || 0,
      requestsPerSecond: requests.rate || 0,
      failureRate: failures.rate || 0,
      checkPassRate: checks.rate || 0,
      maxVirtualUsers: vus.max || vus.value || 0,
      durationMs: data.state?.testRunDurationMs || 0,
    },
    latency: {
      average: duration.avg || 0,
      median: duration.med || 0,
      p90: duration["p(90)"] || 0,
      p95: duration["p(95)"] || 0,
      p99: duration["p(99)"] || 0,
      maximum: duration.max || 0,
    },
    endpoints: endpointNames.map((name) => {
      const values = metricValues(data, `endpoint_${name}_duration`);
      return {
        name,
        average: values.avg || 0,
        p95: values["p(95)"] || 0,
        maximum: values.max || 0,
      };
    }),
    thresholds,
  };

  return {
    stdout: `\nStress report: ${report.overview.status.toUpperCase()} — ${report.overview.requests} requests at ${report.overview.requestsPerSecond.toFixed(1)} req/s\nDashboard data: results/summary.json\n`,
    "results/summary.json": JSON.stringify(report, null, 2),
  };
}
