# University API stress test

This folder contains a k6 ramping stress test and a dependency-free report dashboard.

## Run

1. Start the application stack.
2. Edit `USER_EMAIL` and `USER_PASSWORD` at the top of `stress-test.js` if needed.
3. Run from the repository root:

```bash
mkdir -p performance/results
k6 run performance/stress-test.js
```

The test writes `performance/results/summary.json`. To view it:

```bash
python3 -m http.server 4173 --directory performance
```

Open <http://localhost:4173/report/>. Alternatively, open `report/index.html` directly and use **Load JSON report** to choose `results/summary.json`.

## Configuration

The gateway defaults to `http://localhost:8080`. Override it without editing the test:

```bash
BASE_URL=https://staging.example.com k6 run performance/stress-test.js
```

Stage durations can be overridden with `WARM_UP`, `NORMAL_LOAD`, `STRESS_LOAD`, `SPIKE_LOAD`, and `RECOVERY`. `THINK_TIME_SECONDS` controls the pause between user journeys.

> Run stress tests only against environments you own or have permission to test. The default profile peaks at 100 virtual users and will intentionally exercise rate limits and infrastructure capacity.
