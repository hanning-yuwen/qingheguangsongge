const $ = (id) => document.getElementById(id);
let baseUrl = localStorage.getItem("qh_base_url") || $("baseUrl").value;
$("baseUrl").value = baseUrl;

function showToast(msg) {
  const t = $("toast");
  t.textContent = msg;
  t.style.display = "block";
  setTimeout(() => (t.style.display = "none"), 2600);
}

async function req(path, options = {}) {
  const resp = await fetch(baseUrl + path, {
    headers: { "Content-Type": "application/json" },
    ...options
  });
  const text = await resp.text();
  try { return JSON.parse(text); } catch { return text; }
}

function renderTable(el, rows) {
  if (!rows || rows.length === 0) {
    el.innerHTML = "<tr><td>暂无数据</td></tr>";
    return;
  }
  const keys = Object.keys(rows[0]);
  el.innerHTML = `<tr>${keys.map((k) => `<th>${k}</th>`).join("")}</tr>` +
    rows.map((r) => `<tr>${keys.map((k) => `<td>${r[k] ?? ""}</td>`).join("")}</tr>`).join("");
}

document.querySelectorAll(".nav-btn").forEach((b) => {
  b.onclick = () => {
    document.querySelectorAll(".nav-btn").forEach((x) => x.classList.remove("active"));
    b.classList.add("active");
    document.querySelectorAll(".panel").forEach((p) => p.classList.remove("show"));
    $(b.dataset.panel).classList.add("show");
  };
});

$("saveBaseUrlBtn").onclick = () => {
  baseUrl = $("baseUrl").value.trim();
  localStorage.setItem("qh_base_url", baseUrl);
  showToast("后端地址已保存");
};

$("loginBtn").onclick = async () => {
  const data = await req("/api/user/login", { method: "POST", body: JSON.stringify({ username: $("loginUsername").value, password: $("loginPassword").value }) });
  showToast(typeof data === "string" ? data : JSON.stringify(data));
};
$("addUserBtn").onclick = async () => {
  const data = await req("/api/user/add", {
    method: "POST",
    body: JSON.stringify({ username: $("userUsername").value, password: $("userPassword").value, role: $("userRole").value, name: $("userName").value, phone: $("userPhone").value })
  });
  showToast(typeof data === "string" ? data : JSON.stringify(data));
};
$("loadUsersBtn").onclick = async () => renderTable($("usersTable"), await req("/api/user/list"));

$("addDeviceBtn").onclick = async () => {
  const data = await req("/api/device/add", {
    method: "POST",
    body: JSON.stringify({ name: $("deviceName").value, type: $("deviceType").value, status: $("deviceStatus").value, location: $("deviceLocation").value, ipAddress: $("deviceIp").value, macAddress: $("deviceMac").value })
  });
  showToast(typeof data === "string" ? data : JSON.stringify(data));
};
$("loadDevicesBtn").onclick = async () => renderTable($("devicesTable"), await req("/api/device/list"));

$("querySlotsBtn").onclick = async () => {
  const data = await req(`/api/booking/available?cabinId=${$("slotCabinId").value}&bookingDate=${$("slotDate").value}`);
  $("slotsResult").textContent = JSON.stringify(data, null, 2);
};
$("createBookingBtn").onclick = async () => {
  const data = await req("/api/booking/create", {
    method: "POST",
    body: JSON.stringify({ userId: Number($("bookingUserId").value), cabinId: Number($("bookingCabinId").value), startTime: $("bookingStart").value, endTime: $("bookingEnd").value })
  });
  showToast(JSON.stringify(data));
};
$("loadOrdersByUserBtn").onclick = async () => renderTable($("ordersTable"), await req(`/api/order/user/${$("orderUserId").value}`));
$("loadAllOrdersBtn").onclick = async () => renderTable($("ordersTable"), await req("/api/order/list"));

$("loadCabinsBtn").onclick = async () => renderTable($("cabinsTable"), await req("/api/cabin/list"));
$("openDoorBtn").onclick = async () => {
  const data = await req(`/api/cabin/session/openDoor?userId=${$("openDoorUserId").value}&cabinId=${$("openDoorCabinId").value}`, { method: "POST" });
  showToast(JSON.stringify(data));
};

$("loadEnergyStatsBtn").onclick = async () => {
  $("energyStatsResult").textContent = JSON.stringify(await req("/api/energy/stats"), null, 2);
};
$("loadEnergyByDeviceBtn").onclick = async () => renderTable($("energyTable"), await req(`/api/energy/device/${$("energyDeviceId").value}`));

$("addAlarmBtn").onclick = async () => {
  const now = new Date().toISOString().slice(0, 19).replace("T", " ");
  const data = await req("/api/alarm/add", {
    method: "POST",
    body: JSON.stringify({
      deviceId: $("alarmDeviceId").value,
      deviceName: $("alarmDeviceName").value,
      alarmType: $("alarmType").value,
      alarmLevel: $("alarmLevel").value,
      alarmMessage: $("alarmMessage").value,
      alarmTime: now,
      status: "pending"
    })
  });
  showToast(typeof data === "string" ? data : JSON.stringify(data));
};
$("processAlarmBtn").onclick = async () => {
  const data = await req(`/api/alarm/process?id=${$("processAlarmId").value}&handler=${encodeURIComponent($("processAlarmHandler").value)}&resolution=${encodeURIComponent($("processAlarmResolution").value)}`, { method: "POST" });
  showToast(typeof data === "string" ? data : JSON.stringify(data));
};
$("loadPendingAlarmsBtn").onclick = async () => renderTable($("alarmsTable"), await req("/api/alarm/pending"));
$("loadAlarmsBtn").onclick = async () => renderTable($("alarmsTable"), await req("/api/alarm/list"));

$("refreshDashboardBtn").onclick = async () => {
  const [users, devices, orders, pending] = await Promise.all([
    req("/api/user/list"), req("/api/device/list"), req("/api/order/list"), req("/api/alarm/pending")
  ]);
  $("statUsers").textContent = users.length;
  $("statDevices").textContent = devices.length;
  $("statOrders").textContent = orders.length;
  $("statPendingAlarms").textContent = pending.length;
};

$("loadUsersBtn").click();
