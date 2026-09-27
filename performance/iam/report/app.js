const $ = (selector) => document.querySelector(selector);
const number = new Intl.NumberFormat("en-US", { maximumFractionDigits: 1 });
const compact = new Intl.NumberFormat("en-US", { notation: "compact", maximumFractionDigits: 1 });

function ms(value) { return `${number.format(value || 0)} ms`; }
function pct(value) { return `${number.format((value || 0) * 100)}%`; }
function esc(value) { const node = document.createElement("span"); node.textContent = String(value ?? ""); return node.innerHTML; }

function metricCard(label, valueText, note) {
  return `<article class="metric"><span class="metric-label">${label}</span><strong class="metric-value">${valueText}</strong><span class="metric-note">${note}</span></article>`;
}

function renderMetrics(data) {
  const overview = data.overview || {};
  return [
    metricCard("Total requests", compact.format(overview.requests || 0), `${number.format(overview.requestsPerSecond || 0)} requests / sec`),
    metricCard("P95 latency", ms(data.latency?.p95), `Average ${ms(data.latency?.average)}`),
    metricCard("Valid failures", pct(overview.failureRate), `Raw HTTP failures ${pct(overview.rawHttpFailureRate)}`),
    metricCard("Peak VUs", number.format(overview.maxVirtualUsers || 0), `${pct(overview.checkPassRate)} checks passed`),
  ].join("");
}

function renderLatency(data) {
  const points = [
    ["avg", data.latency?.average],
    ["p50", data.latency?.median],
    ["p90", data.latency?.p90],
    ["p95", data.latency?.p95],
    ["p99", data.latency?.p99],
    ["max", data.latency?.maximum],
  ];
  const max = Math.max(...points.map(([, item]) => item || 0), 1);
  return points.map(([label, item]) => `<div class="bar-row"><span>${label}</span><div class="track"><div class="fill" style="width:${Math.max(2, ((item || 0) / max) * 100)}%"></div></div><span class="bar-value">${ms(item)}</span></div>`).join("");
}

function renderEndpoints(data) {
  return (data.endpoints || []).map((endpoint) => `<div class="endpoint"><div><strong>${esc(endpoint.method)} ${esc(endpoint.name)}</strong><small>${esc(endpoint.path)} · avg ${ms(endpoint.average)} · ${pct(endpoint.failureRate)} failures</small></div><span class="endpoint-p95">${ms(endpoint.p95)}</span></div>`).join("") || "<p class='metric-note'>No endpoint measurements yet.</p>";
}

function renderSpecial(data) {
  const special = data.rateLimiting || {};
  const entries = [
      ["Successful logins", special.loginSuccesses || 0],
      ["Rate-limited logins", special.loginRateLimited || 0],
      ["Expected response", "200 / 429"],
      ["Peak load", data.configuration?.peakVUs || 0]
    ];
  const invariant = null;
  const badge = $("#invariantBadge");
  if (badge) {
    badge.textContent = "observed";
    badge.style.color = "var(--accent)";
  }
  return entries.map(([label, item]) => `<div class="special-item"><strong>${esc(item)}</strong><span>${esc(label)}</span></div>`).join("") +
    `<p class="special-note">${esc(special.note || (data.queryProfile?.note || "Measured workload details are included in the JSON report."))}</p>`;
}

function renderThresholds(data) {
  const thresholds = data.thresholds || [];
  if (!thresholds.length) return "<p class='metric-note'>No measured thresholds yet. This is a starter report.</p>";
  return thresholds.map((threshold) => `<div class="threshold ${threshold.passed ? "pass" : "fail"}"><span class="threshold-icon">${threshold.passed ? "✓" : "!"}</span><div class="threshold-body"><strong>${esc(threshold.metric)}</strong><code>${esc(threshold.rule)}</code></div><strong>${threshold.passed ? "PASS" : "FAIL"}</strong></div>`).join("");
}

function render(data) {
  if (!data.overview || !data.latency || !Array.isArray(data.endpoints)) throw new Error("Unsupported stress-test summary format.");
  const status = data.overview.status || "not-run";
  const passed = status === "passed";
  const notRun = status === "not-run";
  const badge = $("#statusBadge");
  badge.className = `status ${passed ? "passed" : notRun ? "loading" : "failed"}`;
  badge.textContent = passed ? "All gates passed" : notRun ? "Not run" : "Action required";
  $("#subtitle").textContent = `${data.configuration?.scenario || "Stress test"} · ${number.format((data.overview.durationMs || 0) / 1000)} seconds`;
  $("#metrics").innerHTML = renderMetrics(data);
  $("#latencyChart").innerHTML = renderLatency(data);
  $("#endpointList").innerHTML = renderEndpoints(data);
  $("#specialContent").innerHTML = renderSpecial(data);
  $("#thresholdList").innerHTML = renderThresholds(data);
  $("#environment").textContent = data.configuration?.baseUrl || "Environment unavailable";
  $("#generatedAt").textContent = data.generatedAt ? `Generated ${new Date(data.generatedAt).toLocaleString()}` : "No measured run yet";
  $("#errorPanel").classList.add("hidden");
  $("#dashboard").classList.remove("hidden");
}

function showError(error) {
  $("#statusBadge").className = "status loading";
  $("#statusBadge").textContent = "Report needed";
  $("#errorText").textContent = `${error.message} Start a local server or choose the generated JSON file.`;
  $("#dashboard").classList.add("hidden");
  $("#errorPanel").classList.remove("hidden");
}

async function loadFile(file) {
  try { render(JSON.parse(await file.text())); } catch (error) { showError(error); }
}

$("#loadButton").addEventListener("click", () => $("#fileInput").click());
$("#errorLoadButton").addEventListener("click", () => $("#fileInput").click());
$("#fileInput").addEventListener("change", (event) => event.target.files[0] && loadFile(event.target.files[0]));
fetch("../results/summary.json", { cache: "no-store" })
  .then((response) => { if (!response.ok) throw new Error(`Report returned HTTP ${response.status}.`); return response.json(); })
  .then(render)
  .catch(showError);

