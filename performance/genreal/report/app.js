const $ = (selector) => document.querySelector(selector);
const number = new Intl.NumberFormat("en-US", { maximumFractionDigits: 1 });
const compact = new Intl.NumberFormat("en-US", { notation: "compact", maximumFractionDigits: 1 });

function metric(label, value, note) {
  return `<article class="metric"><span class="metric-label">${label}</span><strong class="metric-value">${value}</strong><span class="metric-note">${note}</span></article>`;
}
function ms(value) { return `${number.format(value || 0)} ms`; }
function percent(value) { return `${number.format((value || 0) * 100)}%`; }
function escapeHtml(value) { const node = document.createElement("span"); node.textContent = String(value); return node.innerHTML; }

function render(data) {
  if (!data.overview || !data.latency || !Array.isArray(data.thresholds)) throw new Error("This file is not a supported stress-test summary.");
  const passed = data.overview.status === "passed";
  $("#statusBadge").className = `status ${passed ? "passed" : "failed"}`;
  $("#statusBadge").innerHTML = `<i></i><span>${passed ? "All gates passed" : "Action required"}</span>`;
  $("#subtitle").textContent = `${data.configuration?.scenario || "Stress test"} · ${number.format((data.overview.durationMs || 0) / 1000)} seconds`;
  $("#metrics").innerHTML = [
    metric("Total requests", compact.format(data.overview.requests), `${number.format(data.overview.requestsPerSecond)} requests / sec`),
    metric("P95 latency", ms(data.latency.p95), `Average ${ms(data.latency.average)}`),
    metric("Request failures", percent(data.overview.failureRate), data.overview.failureRate < .02 ? "Within error budget" : "Above error budget"),
    metric("Peak concurrency", number.format(data.overview.maxVirtualUsers), `${percent(data.overview.checkPassRate)} checks passed`),
  ].join("");

  const latency = [["Average",data.latency.average],["Median",data.latency.median],["P90",data.latency.p90],["P95",data.latency.p95],["P99",data.latency.p99],["Maximum",data.latency.maximum]];
  const max = Math.max(...latency.map(([, value]) => value), 1);
  $("#latencyChart").innerHTML = latency.map(([label,value]) => `<div class="bar-row"><span>${label}</span><div class="bar-track"><div class="bar-fill" style="width:${Math.max(2,value/max*100)}%"></div></div><span class="bar-value">${ms(value)}</span></div>`).join("");
  $("#endpointList").innerHTML = (data.endpoints || []).map((item) => `<div class="endpoint"><strong>${escapeHtml(item.name)}</strong><small>avg ${ms(item.average)} · max ${ms(item.maximum)}</small><span class="endpoint-value">${ms(item.p95)}</span></div>`).join("");
  const passedCount = data.thresholds.filter((item) => item.passed).length;
  $("#thresholdCount").textContent = `${passedCount} of ${data.thresholds.length} passed`;
  $("#thresholdList").innerHTML = data.thresholds.map((item) => `<div class="threshold ${item.passed ? "" : "fail"}"><span class="threshold-icon">${item.passed ? "✓" : "!"}</span><div><strong>${escapeHtml(item.metric)}</strong><br><code>${escapeHtml(item.rule)}</code></div><strong>${item.passed ? "PASS" : "FAIL"}</strong></div>`).join("");
  $("#environment").textContent = data.configuration?.baseUrl || "Environment unavailable";
  $("#generatedAt").textContent = `Generated ${new Date(data.generatedAt).toLocaleString()}`;
  $("#errorPanel").classList.add("hidden"); $("#dashboard").classList.remove("hidden");
}

function showError(error) { $("#statusBadge").className="status loading"; $("#statusBadge").innerHTML="<i></i><span>Report needed</span>"; $("#errorText").textContent=`${error.message} Start a local server or choose the generated JSON file.`; $("#dashboard").classList.add("hidden"); $("#errorPanel").classList.remove("hidden"); }
async function loadFile(file) { try { render(JSON.parse(await file.text())); } catch (error) { showError(error); } }
$("#loadButton").addEventListener("click",()=>$("#fileInput").click());
$("#errorLoadButton").addEventListener("click",()=>$("#fileInput").click());
$("#fileInput").addEventListener("change",(event)=>event.target.files[0]&&loadFile(event.target.files[0]));
fetch("../results/summary.json", { cache: "no-store" }).then((response)=>{if(!response.ok)throw new Error(`Report returned HTTP ${response.status}.`);return response.json();}).then(render).catch(showError);
