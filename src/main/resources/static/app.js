const $ = (id) => document.getElementById(id);

let baseUrl = localStorage.getItem("qh_base_url") || "http://localhost:8080";
let currentRole = "";

function showToast(msg) {
  const t = $("toast");
  t.textContent = msg;
  t.style.display = "block";
  setTimeout(() => {
    t.style.display = "none";
  }, 2600);
}

function nowStr() {
  return new Date().toISOString().slice(0, 19).replace("T", " ");
}

function val(id) {
  return $(id).value.trim();
}

function numVal(id) {
  const v = val(id);
  return v === "" ? null : Number(v);
}

function canAccess(roleRequirement) {
  if (!currentRole) return false;
  return roleRequirement === "all" || roleRequirement === currentRole;
}

function parseLoginRole(data) {
  const text = String(data || "");
  if (!text.startsWith("SUCCESS")) return "";
  const role = text.split(":")[1] || "user";
  return role.toLowerCase();
}

async function req(path, options = {}) {
  try {
    const resp = await fetch(baseUrl + path, {
      headers: { "Content-Type": "application/json" },
      ...options
    });
    const text = await resp.text();
    const data = (() => {
      try {
        return JSON.parse(text);
      } catch (e) {
        return text;
      }
    })();
    if (!resp.ok) {
      throw new Error(typeof data === "string" ? data : JSON.stringify(data));
    }
    return data;
  } catch (err) {
    showToast("请求失败: " + err.message);
    throw err;
  }
}

function formatCell(v) {
  if (v === null || v === undefined) return "";
  if (typeof v === "object") return JSON.stringify(v);
  return String(v);
}

function renderTable(el, rows, actionsBuilder) {
  if (!Array.isArray(rows) || rows.length === 0) {
    el.innerHTML = "<tbody><tr><td>暂无数据</td></tr></tbody>";
    return;
  }
  const keys = Object.keys(rows[0]);
  const thead = `<thead><tr>${keys.map((k) => `<th>${k}</th>`).join("")}${actionsBuilder ? "<th>操作</th>" : ""}</tr></thead>`;
  const tbody = rows.map((r, idx) => {
    const actionCell = actionsBuilder ? `<td>${actionsBuilder(r, idx)}</td>` : "";
    return `<tr>${keys.map((k) => `<td>${formatCell(r[k])}</td>`).join("")}${actionCell}</tr>`;
  }).join("");
  el.innerHTML = `${thead}<tbody>${tbody}</tbody>`;
}

function switchPanel(panelId) {
  const panel = $(panelId);
  if (!panel) return;
  if (!canAccess(panel.dataset.role || "all")) {
    showToast("当前角色无权限访问该页面");
    return;
  }
  document.querySelectorAll(".nav-btn").forEach((x) => x.classList.remove("active"));
  document.querySelectorAll(".nav-btn").forEach((b) => {
    if (b.dataset.panel === panelId) b.classList.add("active");
  });
  document.querySelectorAll(".panel").forEach((p) => p.classList.remove("show"));
  panel.classList.add("show");
}

document.querySelectorAll(".nav-btn").forEach((b) => {
  b.onclick = () => switchPanel(b.dataset.panel);
});
document.querySelectorAll("[data-jump]").forEach((b) => {
  b.onclick = () => switchPanel(b.dataset.jump);
});

function applyRoleUI() {
  document.querySelectorAll("[data-role]").forEach((node) => {
    const allowed = canAccess(node.dataset.role || "all");
    node.classList.toggle("hidden-by-role", !allowed);
    if (!allowed && node.classList.contains("panel")) {
      node.classList.remove("show");
    }
  });
  $("sessionBadge").textContent = currentRole ? `已登录: ${currentRole}` : "未登录";
  $("logoutBtn").style.display = currentRole ? "inline-block" : "none";
}

function setLoginGate(locked) {
  $("loginOverlay").style.display = locked ? "flex" : "none";
}

async function enterSystemByRole(role) {
  currentRole = role;
  localStorage.setItem("qh_role", role);
  applyRoleUI();
  setLoginGate(false);
  if (role === "admin") {
    switchPanel("dashboardPanel");
    await Promise.all([
      refreshUsers(),
      refreshDevices(),
      refreshOrders(),
      refreshCabins(),
      refreshEnergy(),
      refreshAlarms(),
      refreshDashboard()
    ]);
  } else {
    switchPanel("bookingPanel");
    await Promise.all([refreshOrders(), refreshCabins()]);
  }
}

async function refreshUsers() {
  const users = await req("/api/user/list");
  renderTable($("usersTable"), users, (row) => `<button class="btn mini" data-user="${row.id}">填充</button>`);
  document.querySelectorAll("[data-user]").forEach((btn) => {
    btn.onclick = () => {
      const row = users.find((x) => String(x.id) === btn.dataset.user);
      if (!row) return;
      $("userId").value = row.id ?? "";
      $("userUsername").value = row.username ?? "";
      $("userRole").value = row.role ?? "";
      $("userName").value = row.name ?? "";
      $("userPhone").value = row.phone ?? "";
      $("deleteUserId").value = row.id ?? "";
    };
  });
}

$("loginBtn").onclick = async () => {
  const data = await req("/api/user/login", {
    method: "POST",
    body: JSON.stringify({ username: val("loginUsername"), password: val("loginPassword") })
  });
  $("loginResult").textContent = String(data);
  const role = parseLoginRole(data);
  if (!role) {
    showToast("登录失败，请检查用户名和密码");
    return;
  }
  showToast(`登录成功，角色: ${role}`);
  await enterSystemByRole(role);
};

$("addUserBtn").onclick = async () => {
  const data = await req("/api/user/add", {
    method: "POST",
    body: JSON.stringify({
      username: val("userUsername"),
      password: val("userPassword"),
      role: val("userRole"),
      name: val("userName"),
      phone: val("userPhone")
    })
  });
  showToast("新增用户: " + data);
  await refreshUsers();
};

$("updateUserBtn").onclick = async () => {
  const id = numVal("userId");
  if (!id) return showToast("请填写用户ID用于更新");
  const data = await req("/api/user/update", {
    method: "POST",
    body: JSON.stringify({
      id,
      username: val("userUsername"),
      password: val("userPassword"),
      role: val("userRole"),
      name: val("userName"),
      phone: val("userPhone")
    })
  });
  showToast("更新用户: " + data);
  await refreshUsers();
};

$("deleteUserBtn").onclick = async () => {
  const id = numVal("deleteUserId");
  if (!id) return showToast("请填写删除用户ID");
  const data = await req(`/api/user/delete?id=${id}`, { method: "POST" });
  showToast("删除用户: " + data);
  await refreshUsers();
};
$("loadUsersBtn").onclick = refreshUsers;

async function refreshDevices(path = "/api/device/list") {
  const rows = await req(path);
  renderTable($("devicesTable"), rows, (row) => `<button class="btn mini" data-device="${row.id}">填充</button>`);
  document.querySelectorAll("[data-device]").forEach((btn) => {
    btn.onclick = () => {
      const row = rows.find((x) => String(x.id) === btn.dataset.device);
      if (!row) return;
      $("deviceId").value = row.id ?? "";
      $("deviceName").value = row.name ?? "";
      $("deviceType").value = row.type ?? "";
      $("deviceStatus").value = row.status ?? "";
      $("deviceLocation").value = row.location ?? "";
      $("deviceIp").value = row.ipAddress ?? "";
      $("deviceMac").value = row.macAddress ?? "";
      $("deleteDeviceId").value = row.id ?? "";
    };
  });
}

$("addDeviceBtn").onclick = async () => {
  const data = await req("/api/device/add", {
    method: "POST",
    body: JSON.stringify({
      name: val("deviceName"),
      type: val("deviceType"),
      status: val("deviceStatus"),
      location: val("deviceLocation"),
      ipAddress: val("deviceIp"),
      macAddress: val("deviceMac")
    })
  });
  showToast("新增设备: " + data);
  await refreshDevices();
};

$("updateDeviceBtn").onclick = async () => {
  const id = numVal("deviceId");
  if (!id) return showToast("请填写设备ID用于更新");
  const data = await req("/api/device/update", {
    method: "POST",
    body: JSON.stringify({
      id,
      name: val("deviceName"),
      type: val("deviceType"),
      status: val("deviceStatus"),
      location: val("deviceLocation"),
      ipAddress: val("deviceIp"),
      macAddress: val("deviceMac")
    })
  });
  showToast("更新设备: " + data);
  await refreshDevices();
};

$("deleteDeviceBtn").onclick = async () => {
  const id = numVal("deleteDeviceId");
  if (!id) return showToast("请填写删除设备ID");
  const data = await req(`/api/device/delete?id=${id}`, { method: "POST" });
  showToast("删除设备: " + data);
  await refreshDevices();
};

$("loadDevicesBtn").onclick = () => refreshDevices("/api/device/list");
$("loadDeviceStatusBtn").onclick = () => refreshDevices("/api/device/status");

$("querySlotsBtn").onclick = async () => {
  const cabinId = numVal("slotCabinId");
  const bookingDate = val("slotDate");
  if (!cabinId || !bookingDate) return showToast("请填写舱位ID和日期");
  const data = await req(`/api/booking/available?cabinId=${cabinId}&bookingDate=${bookingDate}`);
  const slots = Array.isArray(data.availableSlots) ? data.availableSlots : [];
  if (slots.length === 0) {
    $("slotsResult").textContent = "当前日期没有可预约时段，请更换日期或舱位。";
    return;
  }
  $("slotsResult").innerHTML = `可预约时段：<br>${slots.map((s) => `<span class="slot-chip">${s}</span>`).join(" ")}`;
};

$("createBookingBtn").onclick = async () => {
  const data = await req("/api/booking/create", {
    method: "POST",
    body: JSON.stringify({
      userId: numVal("bookingUserId"),
      cabinId: numVal("bookingCabinId"),
      startTime: val("bookingStart"),
      endTime: val("bookingEnd")
    })
  });
  if (data && data.result === "SUCCESS") {
    $("bookingResult").textContent = `预约成功，订单号 ${data.orderId}，费用 ${data.fee}`;
  } else {
    $("bookingResult").textContent = `预约失败：${(data && data.message) || "请检查输入信息"}`;
  }
  showToast("预约请求已完成");
  await refreshOrders();
};

async function refreshOrders(path = "/api/order/list") {
  const rows = await req(path);
  renderTable($("ordersTable"), rows, (row) => `<button class="btn mini" data-order="${row.id}">填充</button>`);
  document.querySelectorAll("[data-order]").forEach((btn) => {
    btn.onclick = () => {
      const row = rows.find((x) => String(x.id) === btn.dataset.order);
      if (!row) return;
      $("orderId").value = row.id ?? "";
      $("orderFormUserId").value = row.userId ?? "";
      $("orderFormCabinId").value = row.cabinId ?? "";
      $("orderStartTime").value = row.startTime ?? "";
      $("orderDuration").value = row.durationMin ?? "";
      $("orderFee").value = row.fee ?? "";
      $("orderStatus").value = row.status ?? "";
      $("deleteOrderId").value = row.id ?? "";
      $("orderDetailId").value = row.id ?? "";
    };
  });
}

$("loadOrdersByUserBtn").onclick = async () => {
  const uid = numVal("orderUserId");
  if (!uid) return showToast("请填写用户ID");
  await refreshOrders(`/api/order/user/${uid}`);
};

$("loadAllOrdersBtn").onclick = () => refreshOrders("/api/order/list");

$("loadOrderDetailBtn").onclick = async () => {
  const id = numVal("orderDetailId");
  if (!id) return showToast("请填写订单ID");
  const data = await req(`/api/order/detail/${id}`);
  $("orderDetailResult").textContent = data
    ? `订单${data.id} | 用户${data.userId} | 舱位${data.cabinId} | 时长${data.durationMin}分钟 | 费用${data.fee} | 状态${data.status}`
    : "未找到该订单";
};

function buildOrderPayload(withId) {
  const payload = {
    userId: numVal("orderFormUserId"),
    cabinId: numVal("orderFormCabinId"),
    startTime: val("orderStartTime"),
    durationMin: numVal("orderDuration"),
    fee: numVal("orderFee"),
    status: numVal("orderStatus")
  };
  if (withId) payload.id = numVal("orderId");
  return payload;
}

$("addOrderBtn").onclick = async () => {
  const data = await req("/api/order/add", {
    method: "POST",
    body: JSON.stringify(buildOrderPayload(false))
  });
  showToast("新增订单: " + data);
  await refreshOrders();
};

$("updateOrderBtn").onclick = async () => {
  if (!numVal("orderId")) return showToast("请填写订单ID用于更新");
  const data = await req("/api/order/update", {
    method: "POST",
    body: JSON.stringify(buildOrderPayload(true))
  });
  showToast("更新订单: " + data);
  await refreshOrders();
};

$("deleteOrderBtn").onclick = async () => {
  const id = numVal("deleteOrderId");
  if (!id) return showToast("请填写删除订单ID");
  const data = await req(`/api/order/delete?id=${id}`, { method: "POST" });
  showToast("删除订单: " + data);
  await refreshOrders();
};

async function refreshCabins() {
  const rows = await req("/api/cabin/list");
  renderTable($("cabinsTable"), rows, (row) => `<button class="btn mini" data-cabin="${row.id}">填充</button>`);
  document.querySelectorAll("[data-cabin]").forEach((btn) => {
    btn.onclick = () => {
      const row = rows.find((x) => String(x.id) === btn.dataset.cabin);
      if (!row) return;
      $("cabinStatusId").value = row.id ?? "";
      $("openDoorCabinId").value = row.id ?? "";
      if (row.status !== undefined && row.status !== null) $("cabinStatusValue").value = row.status;
    };
  });
}

$("loadCabinsBtn").onclick = refreshCabins;

$("updateCabinStatusBtn").onclick = async () => {
  const id = numVal("cabinStatusId");
  const status = numVal("cabinStatusValue");
  if (!id && id !== 0) return showToast("请填写舱位ID");
  if (status === null) return showToast("请填写状态值");
  const data = await req(`/api/cabin/updateStatus?id=${id}&status=${status}`, { method: "POST" });
  showToast("更新舱位状态: " + data);
  await refreshCabins();
};

$("openDoorBtn").onclick = async () => {
  const userId = numVal("openDoorUserId");
  const cabinId = numVal("openDoorCabinId");
  if (!userId || !cabinId) return showToast("请填写用户ID和舱位ID");
  const data = await req(`/api/cabin/session/openDoor?userId=${userId}&cabinId=${cabinId}`, { method: "POST" });
  if (data && data.result === "SUCCESS") {
    $("openDoorResult").textContent = `开门成功，已建立会话 #${data.sessionId}`;
  } else {
    $("openDoorResult").textContent = `开门失败：${(data && data.message) || "请确认预约时段与舱位"}`;
  }
  showToast("开门请求已执行");
  await refreshCabins();
};

async function refreshEnergy(path = "/api/energy/list") {
  const rows = await req(path);
  renderTable($("energyTable"), rows);
}

$("loadEnergyStatsBtn").onclick = async () => {
  const stats = await req("/api/energy/stats");
  $("energyStatsResult").textContent = JSON.stringify(stats, null, 2);
  $("statEnergyUse").textContent = Number(stats.totalConsumption || 0).toFixed(2);
  $("statEnergyGen").textContent = Number(stats.totalGeneration || 0).toFixed(2);
};

$("loadEnergyByDeviceBtn").onclick = async () => {
  const did = val("energyDeviceId");
  if (!did) return showToast("请填写设备ID");
  await refreshEnergy(`/api/energy/device/${did}`);
};

$("addEnergyBtn").onclick = async () => {
  const data = await req("/api/energy/add", {
    method: "POST",
    body: JSON.stringify({
      deviceId: val("energyAddDeviceId"),
      deviceName: val("energyAddDeviceName"),
      powerConsumption: numVal("energyAddConsumption"),
      powerGeneration: numVal("energyAddGeneration"),
      timestamp: val("energyAddTime") || nowStr()
    })
  });
  showToast("新增能耗记录: " + data);
  await refreshEnergy();
  await $("loadEnergyStatsBtn").onclick();
};

$("loadEnergyListBtn").onclick = () => refreshEnergy("/api/energy/list");

async function refreshAlarms(path = "/api/alarm/list") {
  const rows = await req(path);
  renderTable($("alarmsTable"), rows, (row) => `<button class="btn mini" data-alarm="${row.id}">填充</button>`);
  document.querySelectorAll("[data-alarm]").forEach((btn) => {
    btn.onclick = () => {
      const row = rows.find((x) => String(x.id) === btn.dataset.alarm);
      if (!row) return;
      $("processAlarmId").value = row.id ?? "";
      $("alarmStatusId").value = row.id ?? "";
      $("alarmStatusValue").value = row.status ?? "";
    };
  });
}

$("addAlarmBtn").onclick = async () => {
  const data = await req("/api/alarm/add", {
    method: "POST",
    body: JSON.stringify({
      deviceId: val("alarmDeviceId"),
      deviceName: val("alarmDeviceName"),
      alarmType: val("alarmType"),
      alarmLevel: val("alarmLevel"),
      alarmMessage: val("alarmMessage"),
      alarmTime: nowStr(),
      status: "pending"
    })
  });
  showToast("新增告警: " + data);
  await refreshAlarms();
};

$("processAlarmBtn").onclick = async () => {
  const id = numVal("processAlarmId");
  if (!id) return showToast("请填写告警ID");
  const data = await req(
    `/api/alarm/process?id=${id}&handler=${encodeURIComponent(val("processAlarmHandler"))}&resolution=${encodeURIComponent(val("processAlarmResolution"))}`,
    { method: "POST" }
  );
  showToast("处理告警: " + data);
  await refreshAlarms();
};

$("updateAlarmStatusBtn").onclick = async () => {
  const id = numVal("alarmStatusId");
  const status = val("alarmStatusValue");
  if (!id || !status) return showToast("请填写告警ID和状态");
  const data = await req(`/api/alarm/updateStatus?id=${id}&status=${encodeURIComponent(status)}`, { method: "POST" });
  showToast("更新告警状态: " + data);
  await refreshAlarms();
};

$("loadPendingAlarmsBtn").onclick = () => refreshAlarms("/api/alarm/pending");
$("loadAlarmsBtn").onclick = () => refreshAlarms("/api/alarm/list");

async function refreshDashboard() {
  const [users, devices, orders, pending, stats] = await Promise.all([
    req("/api/user/list"),
    req("/api/device/list"),
    req("/api/order/list"),
    req("/api/alarm/pending"),
    req("/api/energy/stats")
  ]);
  $("statUsers").textContent = users.length;
  $("statDevices").textContent = devices.length;
  $("statOrders").textContent = orders.length;
  $("statPendingAlarms").textContent = pending.length;
  $("statEnergyUse").textContent = Number(stats.totalConsumption || 0).toFixed(2);
  $("statEnergyGen").textContent = Number(stats.totalGeneration || 0).toFixed(2);
}

$("refreshDashboardBtn").onclick = refreshDashboard;

$("logoutBtn").onclick = () => {
  currentRole = "";
  localStorage.removeItem("qh_role");
  applyRoleUI();
  setLoginGate(true);
  $("loginResult").textContent = "未登录";
  showToast("已退出登录");
};

function setDefaults() {
  const d = new Date();
  const day = d.toISOString().slice(0, 10);
  $("slotDate").value = day;
  const start = `${day} 09:00:00`;
  const end = `${day} 10:00:00`;
  $("bookingStart").value = start;
  $("bookingEnd").value = end;
  $("orderStartTime").value = start;
  $("energyAddTime").value = nowStr();
}

async function boot() {
  setDefaults();
  applyRoleUI();
  setLoginGate(true);
  const rememberedRole = (localStorage.getItem("qh_role") || "").toLowerCase();
  if (rememberedRole === "admin" || rememberedRole === "user") {
    await enterSystemByRole(rememberedRole);
    showToast("已恢复上次登录状态");
  } else {
    showToast("请先登录");
  }
}

boot();
