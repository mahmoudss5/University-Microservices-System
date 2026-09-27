import http from "k6/http";
import { check, group, sleep } from "k6";
import { Counter, Gauge, Rate, Trend } from "k6/metrics";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const USER_EMAIL = __ENV.USER_EMAIL || "admin@gmail.com";
const USER_PASSWORD = __ENV.USER_PASSWORD || "test1234";
const MAX_VUS = Number(__ENV.MAX_VUS || 200);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.2);

const metrics = {
  login: {
    duration: new Trend("iam_login_duration", true),
    requests: new Counter("iam_login_requests"),
    failures: new Rate("iam_login_failures"),
    statuses2xx: new Counter("iam_login_status_2xx"),
    statuses4xx: new Counter("iam_login_status_4xx"),
    statuses5xx: new Counter("iam_login_status_5xx"),
  },
  profile: {
    duration: new Trend("iam_profile_duration", true),
    requests: new Counter("iam_profile_requests"),
    failures: new Rate("iam_profile_failures"),
    statuses2xx: new Counter("iam_profile_status_2xx"),
    statuses4xx: new Counter("iam_profile_status_4xx"),
    statuses5xx: new Counter("iam_profile_status_5xx"),
  },
  users: {
    duration: new Trend("iam_users_duration", true),
    requests: new Counter("iam_users_requests"),
    failures: new Rate("iam_users_failures"),
    statuses2xx: new Counter("iam_users_status_2xx"),
    statuses4xx: new Counter("iam_users_status_4xx"),
    statuses5xx: new Counter("iam_users_status_5xx"),
  },
};

const validResponseFailures = new Rate("iam_valid_response_failures");
const loginSuccesses = new Counter("iam_login_successes");
const loginRateLimited = new Counter("iam_login_rate_limited");
const setupLoginStatus = new Gauge("iam_setup_login_status");

export const options = {
  scenarios: {
    iam_load: {
      executor: "ramping-vus",
      startVUs: 0,
      stages: [
        { duration: __ENV.WARM_UP || "15s", target: Math.max(1, Math.round(MAX_VUS * 0.1)) },
        { duration: __ENV.NORMAL_LOAD || "25s", target: Math.max(1, Math.round(MAX_VUS * 0.4)) },
        { duration: __ENV.STRESS_LOAD || "35s", target: MAX_VUS },
        { duration: __ENV.RECOVERY || "15s", target: Math.max(1, Math.round(MAX_VUS * 0.1)) },
        { duration: __ENV.COOL_DOWN || "5s", target: 0 },
      ],
      gracefulRampDown: "10s",
    },
  },
  thresholds: {
    checks: ["rate>0.95"],
    iam_valid_response_failures: ["rate<0.02"],
  },
};

function headers(token) {
  return { Authorization: `Bearer ${token}`, "Content-Type": "application/json" };
}

function statusBucket(response, metric) {
  if (response.status >= 200 && response.status < 300) metric.statuses2xx.add(1);
  else if (response.status >= 400 && response.status < 500) metric.statuses4xx.add(1);
  else if (response.status >= 500) metric.statuses5xx.add(1);
}

function observe(name, response, accepted, label) {
  const metric = metrics[name];
  const ok = accepted(response);
  metric.duration.add(response.timings.duration);
  metric.requests.add(1);
  metric.failures.add(ok ? 0 : 1);
  statusBucket(response, metric);
  validResponseFailures.add(ok ? 0 : 1);
  check(response, { [`${label}: accepted response`]: () => ok });
  return ok;
}

function tokenFrom(response) {
  if (response.status !== 200) return "";
  try {
    return response.json("token") || "";
  } catch (_) {
    return "";
  }
}

export function setup() {
  const login = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({ email: USER_EMAIL, password: USER_PASSWORD }),
    { headers: { "Content-Type": "application/json" }, tags: { endpoint: "iam_setup_login" } },
  );
  const ok = check(login, {
    "iam setup login returned 200": (response) => response.status === 200,
    "iam setup login returned a token": (response) => Boolean(tokenFrom(response)),
  });
  setupLoginStatus.add(login.status);
  return {
    token: ok ? tokenFrom(login) : "",
    setupLoginStatus: login.status,
  };
}

export default function (data) {
  group("iam authentication and user management", () => {
    const login = http.post(
      `${BASE_URL}/api/auth/login`,
      JSON.stringify({ email: USER_EMAIL, password: USER_PASSWORD }),
      { headers: { "Content-Type": "application/json" }, tags: { endpoint: "iam_login" } },
    );
    const loginAccepted = observe(
      "login",
      login,
      (response) => response.status === 200 || response.status === 429,
      "iam login",
    );
    if (login.status === 200) loginSuccesses.add(1);
    if (login.status === 429) loginRateLimited.add(1);

    if (!data.token) return;
    observe(
      "profile",
      http.get(`${BASE_URL}/api/users/me`, {
        headers: headers(data.token),
        tags: { endpoint: "iam_users_me" },
      }),
      (response) => response.status === 200,
      "iam current profile",
    );
    observe(
      "users",
      http.get(`${BASE_URL}/api/users`, {
        headers: headers(data.token),
        tags: { endpoint: "iam_users_all" },
      }),
      (response) => response.status === 200,
      "iam users list",
    );
    if (!loginAccepted) sleep(0.05);
  });
  sleep(THINK_TIME_SECONDS);
}

function values(data, name) {
  return data.metrics[name] ? data.metrics[name].values : {};
}

function endpointReport(data, key, label, method, path) {
  const duration = values(data, `iam_${key}_duration`);
  const requests = values(data, `iam_${key}_requests`);
  const failures = values(data, `iam_${key}_failures`);
  return {
    name: label,
    method,
    path,
    requests: requests.count || 0,
    average: duration.avg || 0,
    median: duration.med || 0,
    p90: duration["p(90)"] || 0,
    p95: duration["p(95)"] || 0,
    p99: duration["p(99)"] || 0,
    maximum: duration.max || 0,
    failureRate: failures.rate || 0,
    statusCodes: {
      "2xx": values(data, `iam_${key}_status_2xx`).count || 0,
      "4xx": values(data, `iam_${key}_status_4xx`).count || 0,
      "5xx": values(data, `iam_${key}_status_5xx`).count || 0,
    },
  };
}

function thresholdReport(data) {
  return Object.entries(data.metrics)
    .filter(([, metric]) => metric.thresholds)
    .flatMap(([metricName, metric]) =>
      Object.entries(metric.thresholds).map(([rule, result]) => ({
        metric: metricName,
        rule,
        passed: result.ok === true,
      })),
    );
}

export function handleSummary(data) {
  const duration = values(data, "http_req_duration");
  const requests = values(data, "http_reqs");
  const rawFailures = values(data, "http_req_failed");
  const checks = values(data, "checks");
  const vus = values(data, "vus_max");
  const thresholds = thresholdReport(data);
  const thresholdsPassed = thresholds.every((threshold) => threshold.passed);
  const setupStatus = values(data, "iam_setup_login_status").value || 0;
  const report = {
    schemaVersion: 2,
    service: "iam",
    title: "IAM · Authentication Stress Test",
    generatedAt: new Date().toISOString(),
    configuration: {
      baseUrl: BASE_URL,
      scenario: "200-VU login, current-profile, and admin user-list load",
      peakVUs: MAX_VUS,
      credentials: { email: USER_EMAIL, password: "••••••••" },
    },
    overview: {
      status: thresholdsPassed && setupStatus === 200 ? "passed" : "failed",
      requests: requests.count || 0,
      requestsPerSecond: requests.rate || 0,
      failureRate: values(data, "iam_valid_response_failures").rate || 0,
      rawHttpFailureRate: rawFailures.rate || 0,
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
    endpoints: [
      endpointReport(data, "login", "Login", "POST", "/api/auth/login"),
      endpointReport(data, "profile", "Current profile", "GET", "/api/users/me"),
      endpointReport(data, "users", "Users list", "GET", "/api/users"),
    ],
    rateLimiting: {
      loginSuccesses: values(data, "iam_login_successes").count || 0,
      loginRateLimited: values(data, "iam_login_rate_limited").count || 0,
      note: "HTTP 429 responses from login are reported separately because the gateway intentionally rate-limits authentication traffic.",
    },
    thresholds,
  };

  return {
    stdout: `\nIAM stress report: ${report.overview.status.toUpperCase()} — ${report.overview.requests} requests at ${report.overview.requestsPerSecond.toFixed(1)} req/s\nDashboard data: iam/results/summary.json\n`,
    "results/summary.json": JSON.stringify(report, null, 2),
  };
}
