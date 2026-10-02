# University API stress tests

This folder contains four k6 suites and dependency-free report dashboards. All suites target the API Gateway by default.

## Prerequisites

- Start the Docker Compose stack and ensure the gateway is reachable at `http://localhost:8080`.
- Install [k6](https://grafana.com/docs/k6/latest/set-up/install-k6/).
- Use valid IAM credentials. The academic concurrency suite also needs real student IDs and a disposable course with one available seat.

## General API suite

The general suite exercises authenticated profile/course requests and public department/feedback requests. Run it from the repository root:

```bash
mkdir -p performance/genreal/results
k6 run performance/genreal/stress-test.js
```

It writes `performance/genreal/results/summary.json`. To view the generated dashboard:

```bash
python3 -m http.server 4173 --directory performance
```

Open <http://localhost:4173/genreal/report/>. Alternatively, open `genreal/report/index.html` directly and use **Load JSON report** to choose `genreal/results/summary.json`.

## Configuration

The gateway defaults to `http://localhost:8080`. Override it without editing the test:

```bash
BASE_URL=https://staging.example.com k6 run performance/genreal/stress-test.js
```

The general suite keeps its credentials as local constants at the top of the script, so update those values before running it. Stage durations can be overridden with `WARM_UP`, `NORMAL_LOAD`, `INCREASE_LOAD`, `STRESS_LOAD`, `SPIKE_LOAD`, `RECOVERY`, and `COOL_DOWN`. `THINK_TIME_SECONDS` controls the pause between user journeys, and `MAX_VUS` controls the peak load.

> Run stress tests only against environments you own or have permission to test. The default profiles peak at 200 virtual users and intentionally exercise rate limits and infrastructure capacity.

## Service-specific suites

The repository also contains three independent 200-VU suites. They all target the API Gateway by default (`http://localhost:8080`) and write their own JSON dashboard data.

### Academic Core

The Academic suite exercises the course catalogue and department reads, then starts a one-wave enrollment race with up to 200 VUs. It verifies the course before the race and reports successful enrollments separately from capacity rejections. Set the course to exactly one available seat before running the race.

```bash
cd performance/academic
BASE_URL=http://localhost:8080 \
USER_EMAIL=admin@gmail.com \
USER_PASSWORD=test1234 \
ACADEMIC_COURSE_ID=1 \
ACADEMIC_STUDENT_IDS=101,102,103,104 \
k6 run stress-test.js
```

`ACADEMIC_STUDENT_IDS` should contain real student IDs that already have snapshots in Academic Core. The test does not create users, change course capacity, or clean up enrollments. Use a disposable course and students, or drop the successful enrollment after the run.

### IAM

```bash
cd performance/iam
BASE_URL=http://localhost:8080 \
USER_EMAIL=admin@gmail.com \
USER_PASSWORD=test1234 \
k6 run stress-test.js
```

Login HTTP 429 responses are reported as gateway rate limiting, not hidden as generic failures. Profile and user-list requests still require the setup token.

### Audit Log

```bash
cd performance/audit-log
BASE_URL=http://localhost:8080 \
USER_EMAIL=admin@gmail.com \
USER_PASSWORD=test1234 \
AUDIT_SOURCE=iam-service \
k6 run stress-test.js
```

This suite intentionally measures HTTP queries only. It does not publish Kafka events.

Each suite has a local dashboard at `report/index.html`. Serve the `performance` directory to let the page load its JSON automatically:

```bash
python3 -m http.server 4173 --directory performance
```

Then open `/genreal/report/`, `/academic/report/`, `/iam/report/`, or `/audit-log/report/`.
