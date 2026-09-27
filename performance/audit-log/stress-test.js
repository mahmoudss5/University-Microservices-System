import http from "k6/http";
import { check, group, sleep } from "k6";
import { Counter, Gauge, Rate, Trend } from "k6/metrics";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const USER_EMAIL = __ENV.USER_EMAIL || "admin@gmail.com";
const USER_PASSWORD = __ENV.USER_PASSWORD || "test1234";
const AUDIT_SOURCE = encodeURIComponent(__ENV.AUDIT_SOURCE || "iam-service");
const MAX_VUS = Number(__ENV.MAX_VUS || 200);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.2);

const metrics = {
  list: {
    duration: new Trend("audit_list_duration", true),
    requests: new Counter("audit_list_requests"),
    failures: new Rate("audit_list_failures"),
    statuses2xx: new Counter("audit_list_status_2xx"),
    statuses4xx: new Counter("audit_list_status_4xx"),
    statuses5xx: new Counter("audit_list_status_5xx"),
  },
  filtered: {
    duration: new Trend("audit_filtered_duration", true),
    requests: new Counter("audit_filtered_requests"),
    failures: new Rate("audit_filtered_failures"),
    statuses2xx: new Counter("audit_filtered_status_2xx"),
    statuses4xx: new Counter("audit_filtered_status_4xx"),
    statuses5xx: new Counter("audit_filtered_status_5xx"),
  },
  weekly: {
    duration: new Trend("audit_weekly_duration", true),
    requests: new Counter("audit_weekly_requests"),
    failures: new Rate("audit_weekly_failures"),
    statuses2xx: new Counter("audit_weekly_status_2xx"),
    statuses4xx: new Counter("audit_weekly_status_4xx"),
    statuses5xx: new Counter("audit_weekly_status_5xx"),
  },
};

const validResponseFailures = new Rate("audit_valid_response_failures");
const setupLoginStatus = new Gauge("audit_setup_login_status");

export const options = {
  scenarios: {
    audit_query_load: {
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
    audit_valid_response_failures: ["rate<0.02"],
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

function observe(name, response, label) {
  const metric = metrics[name];
  const ok = response.status === 200;
  metric.duration.add(response.timings.duration);
  metric.requests.add(1);
  metric.failures.add(ok ? 0 : 1);
  statusBucket(response, metric);
  validResponseFailures.add(ok ? 0 : 1);
  check(response, { [`${label}: returned 200`]: () => ok });
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
    { headers: { "Content-Type": "application/json" }, tags: { endpoint: "audit_setup_login" } },
  );
  const ok = check(login, {
    "audit setup login returned 200": (response) => response.status === 200,
    "audit setup login returned a token": (response) => Boolean(tokenFrom(response)),
  });
  setupLoginStatus.add(login.status);
  return {
    token: ok ? tokenFrom(login) : "",
    setupLoginStatus: login.status,
  };
}

export default function (data) {
  if (!data.token) return;
  group("audit log HTTP queries", () => {
    observe(
      "list",
      http.get(`${BASE_URL}/api/audit-logs?page=0&size=20`, {
        headers: headers(data.token),
        tags: { endpoint: "audit_list" },
      }),
      "audit paginated list",
    );
    observe(
      "filtered",
      http.get(`${BASE_URL}/api/audit-logs?page=0&size=20&source=${AUDIT_SOURCE}`, {
        headers: headers(data.token),
        tags: { endpoint: "audit_filtered" },
      }),
      "audit filtered list",
    );
    observe(
      "weekly",
      http.get(`${BASE_URL}/api/audit-logs/last-week-admins-logs`, {
        headers: headers(data.token),
        tags: { endpoint: "audit_weekly_admins" },
      }),
      "audit weekly admin logs",
    );
  });
  sleep(THINK_TIME_SECONDS);
}

function values(data, name) {
  return data.metrics[name] ? data.metrics[name].values : {};
}

function endpointReport(data, key, label, method, path) {
  const duration = values(data, `audit_${key}_duration`);
  const requests = values(data, `audit_${key}_requests`);
  const failures = values(data, `audit_${key}_failures`);
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
      "2xx": values(data, `audit_${key}_status_2xx`).count || 0,
      "4xx": values(data, `audit_${key}_status_4xx`).count || 0,
      "5xx": values(data, `audit_${key}_status_5xx`).count || 0,
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
  const setupStatus = values(data, "audit_setup_login_status").value || 0;
  const thresholdsPassed = thresholds.every((threshold) => threshold.passed);
  const report = {
    schemaVersion: 2,
    service: "audit-log",
    title: "Audit Log · HTTP Query Stress Test",
    generatedAt: new Date().toISOString(),
    configuration: {
      baseUrl: BASE_URL,
      scenario: "200-VU paginated, filtered, and weekly audit-log queries",
      peakVUs: MAX_VUS,
      credentials: { email: USER_EMAIL, password: "••••••••" },
      filterSource: decodeURIComponent(AUDIT_SOURCE),
    },
    overview: {
      status: thresholdsPassed && setupStatus === 200 ? "passed" : "failed",
      requests: requests.count || 0,
      requestsPerSecond: requests.rate || 0,
      failureRate: values(data, "audit_valid_response_failures").rate || 0,
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
      endpointReport(data, "list", "Paginated audit logs", "GET", "/api/audit-logs?page=0&size=20"),
      endpointReport(data, "filtered", "Filtered audit logs", "GET", "/api/audit-logs?page=0&size=20&source=..."),
      endpointReport(data, "weekly", "Weekly admin logs", "GET", "/api/audit-logs/last-week-admins-logs"),
    ],
    queryProfile: {
      filterSource: decodeURIComponent(AUDIT_SOURCE),
      pageSize: 20,
      note: "This suite intentionally tests HTTP reads only; Kafka ingestion is excluded by request.",
    },
    thresholds,
  };

  return {
    stdout: `\nAudit-log stress report: ${report.overview.status.toUpperCase()} — ${report.overview.requests} requests at ${report.overview.requestsPerSecond.toFixed(1)} req/s\nDashboard data: audit-log/results/summary.json\n`,
    "results/summary.json": JSON.stringify(report, null, 2),
  };
}
