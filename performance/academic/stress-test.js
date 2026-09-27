import http from "k6/http";
import { check, group, sleep } from "k6";
import { Counter, Gauge, Rate, Trend } from "k6/metrics";

const BASE_URL = (__ENV.BASE_URL || "http://localhost:8080").replace(/\/$/, "");
const USER_EMAIL = __ENV.USER_EMAIL || "admin@gmail.com";
const USER_PASSWORD = __ENV.USER_PASSWORD || "test1234";
const COURSE_ID = Number(__ENV.ACADEMIC_COURSE_ID || 1);
const STUDENT_IDS = (__ENV.ACADEMIC_STUDENT_IDS || "")
  .split(",")
  .map((value) => Number(value.trim()))
  .filter((value) => Number.isInteger(value) && value > 0);
const EXPECTED_AVAILABLE_SEATS = Number(__ENV.ACADEMIC_EXPECTED_AVAILABLE_SEATS || 1);
const MAX_VUS = Number(__ENV.MAX_VUS || 200);
const THINK_TIME_SECONDS = Number(__ENV.THINK_TIME_SECONDS || 0.2);

const metrics = {
  courseCatalog: {
    duration: new Trend("academic_course_catalog_duration", true),
    requests: new Counter("academic_course_catalog_requests"),
    failures: new Rate("academic_course_catalog_failures"),
    statuses2xx: new Counter("academic_course_catalog_status_2xx"),
    statuses4xx: new Counter("academic_course_catalog_status_4xx"),
    statuses5xx: new Counter("academic_course_catalog_status_5xx"),
  },
  departments: {
    duration: new Trend("academic_departments_duration", true),
    requests: new Counter("academic_departments_requests"),
    failures: new Rate("academic_departments_failures"),
    statuses2xx: new Counter("academic_departments_status_2xx"),
    statuses4xx: new Counter("academic_departments_status_4xx"),
    statuses5xx: new Counter("academic_departments_status_5xx"),
  },
  enrollment: {
    duration: new Trend("academic_enrollment_duration", true),
    requests: new Counter("academic_enrollment_requests"),
    failures: new Rate("academic_enrollment_failures"),
    statuses2xx: new Counter("academic_enrollment_status_2xx"),
    statuses4xx: new Counter("academic_enrollment_status_4xx"),
    statuses5xx: new Counter("academic_enrollment_status_5xx"),
  },
};

const validResponseFailures = new Rate("academic_valid_response_failures");
const enrollmentSuccesses = new Counter("academic_enrollment_successes");
const capacityRejections = new Counter("academic_capacity_rejections");
const unknownEnrollmentResponses = new Counter("academic_unknown_enrollment_responses");
const setupLoginStatus = new Gauge("academic_setup_login_status");
const setupCourseStatus = new Gauge("academic_setup_course_status");
const setupAvailableSeats = new Gauge("academic_setup_available_seats");
const setupMaxStudents = new Gauge("academic_setup_max_students");
const setupEnrolledCount = new Gauge("academic_setup_enrolled_count");
const setupStudentCount = new Gauge("academic_setup_student_count");

export const options = {
  scenarios: {
    academic_catalog_load: {
      executor: "ramping-vus",
      exec: "catalogJourney",
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
    academic_enrollment_race: {
      executor: "per-vu-iterations",
      exec: "enrollmentRace",
      vus: MAX_VUS,
      iterations: 1,
      startTime: __ENV.ENROLLMENT_START || "110s",
      maxDuration: __ENV.ENROLLMENT_MAX_DURATION || "45s",
    },
  },
  thresholds: {
    checks: ["rate>0.95"],
    academic_valid_response_failures: ["rate<0.02"],
    academic_enrollment_successes: ["count<=1"],
  },
};

function authHeaders(token) {
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

function isCapacityRejection(response) {
  if (response.status === 400 || response.status === 409) return true;
  return response.status === 500 && String(response.body || "").toLowerCase().includes("course is full");
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
    { headers: { "Content-Type": "application/json" }, tags: { endpoint: "academic_setup_login" } },
  );
  const loginOk = check(login, {
    "academic setup login returned 200": (response) => response.status === 200,
    "academic setup login returned a token": (response) => Boolean(tokenFrom(response)),
  });

  const token = loginOk ? tokenFrom(login) : "";
  setupLoginStatus.add(login.status);
  let courseState = { status: 0, availableSeats: null, maxStudents: null, enrolledCount: null };
  if (token) {
    const course = http.get(`${BASE_URL}/api/courses/${COURSE_ID}`, {
      headers: authHeaders(token),
      tags: { endpoint: "academic_setup_course" },
    });
    const body = course.status === 200 ? course.json() : {};
    const maxStudents = Number(body?.maxStudents);
    const enrolledCount = Number(body?.enrolledCount);
    courseState = {
      status: course.status,
      availableSeats: Number.isFinite(maxStudents) && Number.isFinite(enrolledCount)
        ? maxStudents - enrolledCount
        : null,
      maxStudents: Number.isFinite(maxStudents) ? maxStudents : null,
      enrolledCount: Number.isFinite(enrolledCount) ? enrolledCount : null,
    };
  }

  setupCourseStatus.add(courseState.status);
  setupAvailableSeats.add(courseState.availableSeats === null ? -1 : courseState.availableSeats);
  setupMaxStudents.add(courseState.maxStudents === null ? -1 : courseState.maxStudents);
  setupEnrolledCount.add(courseState.enrolledCount === null ? -1 : courseState.enrolledCount);
  setupStudentCount.add(STUDENT_IDS.length);

  return {
    token,
    setupLoginStatus: login.status,
    courseState,
    studentIds: STUDENT_IDS,
  };
}

export function catalogJourney(data) {
  if (!data.token) return;
  group("academic catalogue", () => {
    observe(
      "courseCatalog",
      http.get(`${BASE_URL}/api/courses/all`, {
        headers: authHeaders(data.token),
        tags: { endpoint: "academic_courses_all" },
      }),
      (response) => response.status === 200,
      "academic courses all",
    );
    observe(
      "departments",
      http.get(`${BASE_URL}/api/departments/all`, {
        headers: authHeaders(data.token),
        tags: { endpoint: "academic_departments_all" },
      }),
      (response) => response.status === 200,
      "academic departments all",
    );
  });
  sleep(THINK_TIME_SECONDS);
}

export function enrollmentRace(data) {
  if (!data.token || data.studentIds.length === 0) return;

  const studentId = data.studentIds[(__VU - 1) % data.studentIds.length];
  const response = http.post(
    `${BASE_URL}/api/enrolled-courses`,
    JSON.stringify({ studentId, courseId: COURSE_ID }),
    {
      headers: authHeaders(data.token),
      tags: { endpoint: "academic_enrollment_race" },
    },
  );

  const successful = response.status === 200 || response.status === 201;
  const capacityRejected = isCapacityRejection(response);
  const recognized = successful || capacityRejected;

  observe("enrollment", response, () => recognized, "academic enrollment race");
  if (successful) enrollmentSuccesses.add(1);
  else if (capacityRejected) capacityRejections.add(1);
  else unknownEnrollmentResponses.add(1);
}

function values(data, name) {
  return data.metrics[name] ? data.metrics[name].values : {};
}

function endpointReport(data, key, label, method, path) {
  const duration = values(data, `academic_${key}_duration`);
  const metric = metrics[key];
  const requests = values(data, `academic_${key}_requests`);
  const failures = values(data, `academic_${key}_failures`);
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
      "2xx": values(data, `academic_${key}_status_2xx`).count || 0,
      "4xx": values(data, `academic_${key}_status_4xx`).count || 0,
      "5xx": values(data, `academic_${key}_status_5xx`).count || 0,
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
  const courseState = {
    status: values(data, "academic_setup_course_status").value || 0,
    availableSeats: values(data, "academic_setup_available_seats").value ?? null,
    maxStudents: values(data, "academic_setup_max_students").value ?? null,
    enrolledCount: values(data, "academic_setup_enrolled_count").value ?? null,
  };
  const studentCount = values(data, "academic_setup_student_count").value || 0;
  const enrollmentRequestCount = values(data, "academic_enrollment_requests").count || 0;
  const successfulEnrollments = values(data, "academic_enrollment_successes").count || 0;
  const capacityRejectionCount = values(data, "academic_capacity_rejections").count || 0;
  const unknownEnrollmentResponseCount = values(data, "academic_unknown_enrollment_responses").count || 0;
  const readyForOneSeatRace =
    courseState.status === 200 &&
    courseState.availableSeats === EXPECTED_AVAILABLE_SEATS &&
    studentCount >= 2;
  const invariantPassed =
    readyForOneSeatRace &&
    enrollmentRequestCount > 0 &&
    unknownEnrollmentResponseCount === 0 &&
    successfulEnrollments <= EXPECTED_AVAILABLE_SEATS;
  const thresholdsPassed = thresholds.every((threshold) => threshold.passed);

  const report = {
    schemaVersion: 2,
    service: "academic-core",
    title: "Academic Core · Concurrency Stress Test",
    generatedAt: new Date().toISOString(),
    configuration: {
      baseUrl: BASE_URL,
      scenario: "200-VU catalogue load followed by one-wave enrollment race",
      peakVUs: MAX_VUS,
      credentials: { email: USER_EMAIL, password: "••••••••" },
      courseId: COURSE_ID,
      studentIdsConfigured: studentCount,
      expectedAvailableSeats: EXPECTED_AVAILABLE_SEATS,
    },
    overview: {
      status: thresholdsPassed && invariantPassed ? "passed" : "failed",
      requests: requests.count || 0,
      requestsPerSecond: requests.rate || 0,
      failureRate: values(data, "academic_valid_response_failures").rate || 0,
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
      endpointReport(data, "courseCatalog", "Course catalogue", "GET", "/api/courses/all"),
      endpointReport(data, "departments", "Departments", "GET", "/api/departments/all"),
      endpointReport(data, "enrollment", "Enrollment race", "POST", "/api/enrolled-courses"),
    ],
    concurrency: {
      courseId: COURSE_ID,
      courseStateBeforeRace: courseState,
      studentIdsConfigured: studentCount,
      attempts: enrollmentRequestCount,
      successfulEnrollments,
      capacityRejections: capacityRejectionCount,
      unknownResponses: unknownEnrollmentResponseCount,
      expectedAvailableSeats: EXPECTED_AVAILABLE_SEATS,
      readyForOneSeatRace,
      invariant: invariantPassed ? "passed" : (readyForOneSeatRace ? "oversubscribed" : "not-ready"),
      invariantPassed,
      note: "Capacity rejections are expected. Any successful enrollments above the available-seat limit indicate oversubscription.",
    },
    thresholds,
  };

  return {
    stdout: `\nAcademic stress report: ${report.overview.status.toUpperCase()} — ${report.overview.requests} requests at ${report.overview.requestsPerSecond.toFixed(1)} req/s\nConcurrency invariant: ${report.concurrency.invariant}\nDashboard data: academic/results/summary.json\n`,
    "results/summary.json": JSON.stringify(report, null, 2),
  };
}
