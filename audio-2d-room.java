import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

class Audio2DRoom {
    private static final String PAGE = """
            <!doctype html>
            <html lang="en">
            <head>
              <meta charset="utf-8">
              <meta name="viewport" content="width=device-width, initial-scale=1">
              <title>RoomSketch — 2D Room Planner</title>
              <style>
                :root {
                  color-scheme: light;
                  font-family: Inter, ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
                  color: #202b3c;
                  background: #f3f5f8;
                  font-synthesis: none;
                }
                * { box-sizing: border-box; }
                body { margin: 0; min-width: 320px; min-height: 100vh; }
                button, input { font: inherit; }
                button { cursor: pointer; }
                .topbar {
                  min-height: 68px; display: flex; align-items: center; justify-content: space-between;
                  gap: 16px; padding: 12px 24px; background: #fff; border-bottom: 1px solid #e4e8ef;
                }
                .brand { display: flex; align-items: center; gap: 11px; font-size: 17px; font-weight: 750; letter-spacing: -.3px; }
                .brand-mark {
                  width: 34px; height: 34px; display: grid; place-items: center; border-radius: 10px;
                  color: #fff; background: #5268e8; font-size: 19px;
                }
                .top-actions { display: flex; gap: 9px; }
                .button {
                  min-height: 38px; padding: 8px 13px; border: 1px solid #dce1ea; border-radius: 8px;
                  background: #fff; color: #354156; font-size: 13px; font-weight: 650;
                }
                .button:hover { background: #f7f8fb; }
                .button.primary { border-color: #5268e8; background: #5268e8; color: #fff; }
                .button.primary:hover { background: #4359d8; }
                .layout {
                  width: min(1500px, 100%); min-height: calc(100vh - 68px); margin: 0 auto; padding: 22px;
                  display: grid; grid-template-columns: 220px minmax(0, 1fr) 220px; gap: 18px;
                }
                .panel, .workspace { background: #fff; border: 1px solid #e4e8ef; border-radius: 12px; }
                .panel { padding: 17px; align-self: start; }
                h1 { margin: 0 0 5px; font-size: 19px; letter-spacing: -.35px; }
                h2 { margin: 0 0 13px; color: #344158; font-size: 13px; font-weight: 750; }
                .subheading { margin: 0 0 17px; color: #788397; font-size: 12px; line-height: 1.5; }
                .palette { display: grid; gap: 8px; }
                .item {
                  min-height: 51px; display: flex; align-items: center; gap: 11px; padding: 8px 10px;
                  border: 1px solid #e8ebf1; border-radius: 9px; background: #fff; color: #354156;
                  text-align: left; transition: border-color .15s, background .15s;
                }
                .item:hover { border-color: #aab4f5; background: #f8f8ff; }
                .item:active { cursor: grabbing; }
                .item-icon { width: 34px; height: 29px; display: grid; place-items: center; border-radius: 6px; font-size: 17px; }
                .item-name { font-size: 13px; font-weight: 650; }
                .item-size { display: block; margin-top: 2px; color: #8791a2; font-size: 11px; font-weight: 450; }
                .workspace { min-width: 0; min-height: 560px; display: flex; flex-direction: column; overflow: hidden; }
                .workspace-head {
                  min-height: 58px; display: flex; align-items: center; justify-content: space-between;
                  gap: 12px; padding: 12px 17px; border-bottom: 1px solid #edf0f4;
                }
                .workspace-title { font-size: 13px; font-weight: 750; }
                .room-size { display: flex; align-items: center; gap: 7px; color: #788397; font-size: 12px; }
                .room-size input {
                  width: 57px; height: 32px; padding: 4px 7px; border: 1px solid #dce1ea;
                  border-radius: 7px; color: #344158; text-align: center;
                }
                .unit { margin-left: -4px; }
                .canvas-wrap {
                  flex: 1; min-height: 460px; display: grid; place-items: center; overflow: auto;
                  padding: 26px; background-color: #fafbfc;
                  background-image: radial-gradient(#dfe3eb 1px, transparent 1px); background-size: 18px 18px;
                }
                svg#room {
                  display: block; width: min(100%, 760px); max-height: 70vh; height: auto;
                  overflow: visible; touch-action: none; box-shadow: 0 5px 22px #1f2c4517;
                }
                .room-object { cursor: grab; }
                .room-object:active { cursor: grabbing; }
                .room-object.selected > rect { stroke: #5268e8; stroke-width: 3; }
                .room-object text { fill: #28334a; font-size: 11px; font-weight: 700; text-anchor: middle; dominant-baseline: central; pointer-events: none; }
                .hint { padding: 11px 16px; border-top: 1px solid #edf0f4; color: #8791a2; font-size: 11px; text-align: center; }
                .details-empty { color: #8791a2; font-size: 12px; line-height: 1.55; }
                .selection-name { margin-bottom: 5px; font-size: 15px; font-weight: 750; }
                .selection-meta { margin-bottom: 15px; color: #788397; font-size: 12px; }
                .detail-actions { display: grid; gap: 8px; }
                .detail-actions .button { width: 100%; }
                .divider { height: 1px; margin: 17px 0; background: #edf0f4; }
                .stat { display: flex; justify-content: space-between; gap: 10px; padding: 6px 0; color: #788397; font-size: 12px; }
                .stat strong { color: #354156; font-weight: 650; }
                .status { min-height: 17px; margin-top: 13px; color: #68758a; font-size: 11px; line-height: 1.45; }
                .status.error { color: #b42318; }
                @media (max-width: 980px) {
                  .layout { grid-template-columns: 190px minmax(0, 1fr); }
                  .inspector { grid-column: 1 / -1; display: grid; grid-template-columns: 1fr 1fr; gap: 18px; }
                  .inspector h2 { grid-column: 1 / -1; margin-bottom: -9px; }
                }
                @media (max-width: 650px) {
                  .topbar { padding: 11px 14px; }
                  .top-actions .button { padding: 7px 9px; }
                  .layout { grid-template-columns: 1fr; padding: 12px; gap: 12px; }
                  .workspace { grid-row: 1; min-height: 490px; }
                  .palette { grid-template-columns: repeat(2, minmax(0, 1fr)); }
                  .panel { padding: 14px; }
                  .inspector { grid-column: auto; }
                  .canvas-wrap { padding: 16px; min-height: 390px; }
                  .workspace-head { align-items: flex-start; }
                  .room-size { gap: 4px; }
                }
              </style>
            </head>
            <body>
              <header class="topbar">
                <div class="brand"><span class="brand-mark" aria-hidden="true">⌂</span>RoomSketch</div>
                <div class="top-actions">
                  <button class="button" id="export-button" type="button">Export layout</button>
                  <button class="button primary" id="save-button" type="button">Save layout</button>
                </div>
              </header>
              <main class="layout">
                <section class="panel" aria-labelledby="furniture-heading">
                  <h1>Build your room</h1>
                  <p class="subheading">Drag a piece into the room. Move, rotate, and arrange it to fit.</p>
                  <h2 id="furniture-heading">Furniture &amp; fixtures</h2>
                  <div class="palette" id="palette"></div>
                </section>
                <section class="workspace" aria-label="Room editor">
                  <div class="workspace-head">
                    <span class="workspace-title">ROOM PLAN</span>
                    <div class="room-size" aria-label="Room dimensions in meters">
                      <label for="room-width">Width</label><input id="room-width" type="number" min="3" max="12" step="0.5" value="6"><span class="unit">m</span>
                      <label for="room-height">Depth</label><input id="room-height" type="number" min="3" max="12" step="0.5" value="4"><span class="unit">m</span>
                    </div>
                  </div>
                  <div class="canvas-wrap" id="canvas-wrap">
                    <svg id="room" role="application" aria-label="Room floor plan. Drag furniture to move it.">
                      <defs>
                        <pattern id="grid" width="60" height="60" patternUnits="userSpaceOnUse">
                          <path d="M 60 0 L 0 0 0 60" fill="none" stroke="#d7dce5" stroke-width="1"/>
                          <path d="M 30 0 V 60 M 0 30 H 60" fill="none" stroke="#eceff4" stroke-width="1"/>
                        </pattern>
                      </defs>
                      <rect id="floor" x="0" y="0" fill="#fff" stroke="#354156" stroke-width="5"/>
                      <rect id="grid-overlay" x="0" y="0" fill="url(#grid)" pointer-events="none"/>
                      <g id="objects"></g>
                    </svg>
                  </div>
                  <div class="hint">Grid squares are 0.5 m · Select an item to rotate or remove it</div>
                </section>
                <aside class="panel inspector" aria-labelledby="inspector-heading">
                  <h2 id="inspector-heading">Selection</h2>
                  <div>
                    <div id="selection" class="details-empty">Select an item in the room to see its size and actions.</div>
                    <div class="detail-actions" id="detail-actions" hidden>
                      <button class="button" id="rotate-button" type="button">Rotate 90°</button>
                      <button class="button" id="delete-button" type="button">Remove item</button>
                    </div>
                  </div>
                  <div>
                    <div class="divider"></div>
                    <h2>Room summary</h2>
                    <div class="stat"><span>Floor area</span><strong id="area">24 m²</strong></div>
                    <div class="stat"><span>Items placed</span><strong id="item-count">0</strong></div>
                    <div class="status" id="status" role="status" aria-live="polite"></div>
                  </div>
                </aside>
              </main>
              <script>
                "use strict";
                const SCALE = 60;
                const STORAGE_KEY = "roomsketch-layout-v1";
                const catalog = {
                  sofa:   { name: "Sofa",   width: 1.8, height: 0.8, color: "#d9d4fb", icon: "🛋️" },
                  bed:    { name: "Bed",    width: 2.0, height: 1.4, color: "#cfe5fb", icon: "🛏️" },
                  table:  { name: "Table",  width: 1.2, height: 0.8, color: "#f7dfb4", icon: "▦" },
                  chair:  { name: "Chair",  width: 0.6, height: 0.6, color: "#f6d2c8", icon: "🪑" },
                  plant:  { name: "Plant",  width: 0.5, height: 0.5, color: "#cce8d6", icon: "🪴" },
                  door:   { name: "Door",   width: 0.9, height: 0.15, color: "#e8c89f", icon: "↗" },
                  window: { name: "Window", width: 1.2, height: 0.15, color: "#b9e3ef", icon: "▤" }
                };
                const svg = document.getElementById("room");
                const objectLayer = document.getElementById("objects");
                const selection = document.getElementById("selection");
                const status = document.getElementById("status");
                const state = { width: 6, height: 4, objects: [], selectedId: null, drag: null };
                const svgNS = "http://www.w3.org/2000/svg";

                function showStatus(message, isError = false) {
                  status.textContent = message;
                  status.classList.toggle("error", isError);
                }

                function clamp(value, min, max) {
                  return Math.max(min, Math.min(max, value));
                }

                function dimensions(item) {
                  const definition = catalog[item.type];
                  return item.rotation % 180 === 0
                    ? { width: definition.width, height: definition.height }
                    : { width: definition.height, height: definition.width };
                }

                function constrain(item) {
                  const size = dimensions(item);
                  item.x = clamp(item.x, 0, Math.max(0, state.width - size.width));
                  item.y = clamp(item.y, 0, Math.max(0, state.height - size.height));
                }

                function render() {
                  const width = state.width * SCALE;
                  const height = state.height * SCALE;
                  svg.setAttribute("viewBox", `0 0 ${width} ${height}`);
                  svg.setAttribute("width", width);
                  svg.setAttribute("height", height);
                  for (const id of ["floor", "grid-overlay"]) {
                    document.getElementById(id).setAttribute("width", width);
                    document.getElementById(id).setAttribute("height", height);
                  }
                  objectLayer.replaceChildren();

                  for (const item of state.objects) {
                    const definition = catalog[item.type];
                    const w = definition.width * SCALE;
                    const h = definition.height * SCALE;
                    const group = document.createElementNS(svgNS, "g");
                    group.setAttribute("class", `room-object${item.id === state.selectedId ? " selected" : ""}`);
                    group.setAttribute("data-id", item.id);
                    group.setAttribute("transform", `translate(${item.x * SCALE} ${item.y * SCALE}) rotate(${item.rotation} ${w / 2} ${h / 2})`);
                    const rect = document.createElementNS(svgNS, "rect");
                    rect.setAttribute("width", w);
                    rect.setAttribute("height", h);
                    rect.setAttribute("rx", "5");
                    rect.setAttribute("fill", definition.color);
                    rect.setAttribute("stroke", "#788397");
                    rect.setAttribute("stroke-width", "1.5");
                    group.append(rect);
                    const label = document.createElementNS(svgNS, "text");
                    label.setAttribute("x", w / 2);
                    label.setAttribute("y", h / 2);
                    label.textContent = definition.name;
                    group.append(label);
                    objectLayer.append(group);
                  }

                  document.getElementById("area").textContent = `${formatNumber(state.width * state.height)} m²`;
                  document.getElementById("item-count").textContent = String(state.objects.length);
                  updateSelection();
                }

                function formatNumber(value) {
                  return Number(value.toFixed(2)).toString();
                }

                function updateSelection() {
                  const item = state.objects.find(candidate => candidate.id === state.selectedId);
                  const actions = document.getElementById("detail-actions");
                  if (!item) {
                    selection.className = "details-empty";
                    selection.textContent = "Select an item in the room to see its size and actions.";
                    actions.hidden = true;
                    return;
                  }
                  const definition = catalog[item.type];
                  const size = dimensions(item);
                  selection.className = "";
                  selection.replaceChildren();
                  const name = document.createElement("div");
                  name.className = "selection-name";
                  name.textContent = definition.name;
                  const meta = document.createElement("div");
                  meta.className = "selection-meta";
                  meta.textContent = `${formatNumber(size.width)} × ${formatNumber(size.height)} m · ${item.rotation}°`;
                  selection.append(name, meta);
                  actions.hidden = false;
                }

                function pointInRoom(event) {
                  const point = svg.createSVGPoint();
                  point.x = event.clientX;
                  point.y = event.clientY;
                  const transformed = point.matrixTransform(svg.getScreenCTM().inverse());
                  return { x: transformed.x / SCALE, y: transformed.y / SCALE };
                }

                function createId() {
                  return globalThis.crypto?.randomUUID
                    ? globalThis.crypto.randomUUID()
                    : `item-${Date.now()}-${Math.random().toString(16).slice(2)}`;
                }

                function addItem(type, x, y) {
                  const definition = catalog[type];
                  if (!definition) return;
                  const item = {
                    id: createId(),
                    type,
                    x: x - definition.width / 2,
                    y: y - definition.height / 2,
                    rotation: 0
                  };
                  constrain(item);
                  state.objects.push(item);
                  state.selectedId = item.id;
                  render();
                }

                function parseLayout(raw) {
                  const saved = JSON.parse(raw);
                  if (!saved || typeof saved !== "object" || !Array.isArray(saved.objects)) {
                    throw new Error("Saved layout has an invalid format.");
                  }
                  const width = Number(saved.width);
                  const height = Number(saved.height);
                  if (!Number.isFinite(width) || !Number.isFinite(height) ||
                      width < 3 || width > 12 || height < 3 || height > 12) {
                    throw new Error("Saved room dimensions are invalid.");
                  }
                  const objects = saved.objects.map(item => {
                    if (!item || !catalog[item.type] || !Number.isFinite(item.x) ||
                        !Number.isFinite(item.y) || !Number.isFinite(item.rotation)) {
                      throw new Error("A saved furniture item is invalid.");
                    }
                    return {
                      id: typeof item.id === "string" ? item.id : createId(),
                      type: item.type,
                      x: item.x,
                      y: item.y,
                      rotation: ((Math.round(item.rotation / 90) * 90) % 360 + 360) % 360
                    };
                  });
                  return { width, height, objects };
                }

                function saveLayout() {
                  try {
                    localStorage.setItem(STORAGE_KEY, JSON.stringify({
                      width: state.width, height: state.height, objects: state.objects
                    }));
                    showStatus("Layout saved in this browser.");
                  } catch (error) {
                    showStatus(`Could not save layout: ${error.message}`, true);
                  }
                }

                function exportLayout() {
                  const data = JSON.stringify({ width: state.width, height: state.height, objects: state.objects }, null, 2);
                  const url = URL.createObjectURL(new Blob([data], { type: "application/json" }));
                  const link = document.createElement("a");
                  link.href = url;
                  link.download = "room-layout.json";
                  link.click();
                  URL.revokeObjectURL(url);
                  showStatus("Layout exported as room-layout.json.");
                }

                function loadLayout() {
                  try {
                    const raw = localStorage.getItem(STORAGE_KEY);
                    if (!raw) return;
                    const saved = parseLayout(raw);
                    state.width = saved.width;
                    state.height = saved.height;
                    state.objects = saved.objects;
                    document.getElementById("room-width").value = state.width;
                    document.getElementById("room-height").value = state.height;
                  } catch (error) {
                    showStatus(`Could not load saved layout: ${error.message}`, true);
                  }
                }

                for (const [type, definition] of Object.entries(catalog)) {
                  const button = document.createElement("button");
                  button.type = "button";
                  button.className = "item";
                  button.draggable = true;
                  button.dataset.type = type;
                  button.setAttribute("aria-label", `Drag ${definition.name}, ${definition.width} by ${definition.height} meters, into the room`);
                  const icon = document.createElement("span");
                  icon.className = "item-icon";
                  icon.style.background = definition.color;
                  icon.textContent = definition.icon;
                  const text = document.createElement("span");
                  text.className = "item-name";
                  text.append(document.createTextNode(definition.name));
                  const size = document.createElement("span");
                  size.className = "item-size";
                  size.textContent = `${definition.width} × ${definition.height} m`;
                  text.append(size);
                  button.append(icon, text);
                  button.addEventListener("dragstart", event => {
                    event.dataTransfer.setData("text/plain", type);
                    event.dataTransfer.effectAllowed = "copy";
                  });
                  button.addEventListener("click", () => addItem(type, state.width / 2, state.height / 2));
                  document.getElementById("palette").append(button);
                }

                svg.addEventListener("dragover", event => {
                  event.preventDefault();
                  event.dataTransfer.dropEffect = "copy";
                });
                svg.addEventListener("drop", event => {
                  event.preventDefault();
                  const type = event.dataTransfer.getData("text/plain");
                  const point = pointInRoom(event);
                  addItem(type, point.x, point.y);
                });
                svg.addEventListener("pointerdown", event => {
                  const group = event.target.closest(".room-object");
                  if (!group) {
                    state.selectedId = null;
                    render();
                    return;
                  }
                  const item = state.objects.find(candidate => candidate.id === group.dataset.id);
                  if (!item) return;
                  state.selectedId = item.id;
                  const point = pointInRoom(event);
                  state.drag = { id: item.id, x: item.x, y: item.y, pointerX: point.x, pointerY: point.y };
                  svg.setPointerCapture(event.pointerId);
                  render();
                });
                svg.addEventListener("pointermove", event => {
                  if (!state.drag) return;
                  const point = pointInRoom(event);
                  const item = state.objects.find(candidate => candidate.id === state.drag.id);
                  if (!item) return;
                  item.x = state.drag.x + point.x - state.drag.pointerX;
                  item.y = state.drag.y + point.y - state.drag.pointerY;
                  constrain(item);
                  render();
                });
                svg.addEventListener("pointerup", () => { state.drag = null; });
                svg.addEventListener("pointercancel", () => { state.drag = null; });
                document.getElementById("rotate-button").addEventListener("click", () => {
                  const item = state.objects.find(candidate => candidate.id === state.selectedId);
                  if (!item) return;
                  const centerX = item.x + dimensions(item).width / 2;
                  const centerY = item.y + dimensions(item).height / 2;
                  item.rotation = (item.rotation + 90) % 360;
                  const size = dimensions(item);
                  item.x = centerX - size.width / 2;
                  item.y = centerY - size.height / 2;
                  constrain(item);
                  render();
                });
                document.getElementById("delete-button").addEventListener("click", () => {
                  state.objects = state.objects.filter(item => item.id !== state.selectedId);
                  state.selectedId = null;
                  render();
                });
                document.addEventListener("keydown", event => {
                  if ((event.key === "Delete" || event.key === "Backspace") &&
                      !["INPUT", "TEXTAREA"].includes(document.activeElement.tagName) && state.selectedId) {
                    state.objects = state.objects.filter(item => item.id !== state.selectedId);
                    state.selectedId = null;
                    render();
                  }
                });

                function updateRoomSize() {
                  const widthInput = document.getElementById("room-width");
                  const heightInput = document.getElementById("room-height");
                  const width = Number(widthInput.value);
                  const height = Number(heightInput.value);
                  if (!Number.isFinite(width) || !Number.isFinite(height) ||
                      width < 3 || width > 12 || height < 3 || height > 12) {
                    showStatus("Room width and depth must be between 3 m and 12 m.", true);
                    widthInput.value = state.width;
                    heightInput.value = state.height;
                    return;
                  }
                  state.width = width;
                  state.height = height;
                  for (const item of state.objects) constrain(item);
                  showStatus("");
                  render();
                }
                document.getElementById("room-width").addEventListener("change", updateRoomSize);
                document.getElementById("room-height").addEventListener("change", updateRoomSize);
                document.getElementById("save-button").addEventListener("click", saveLayout);
                document.getElementById("export-button").addEventListener("click", exportLayout);

                loadLayout();
                render();
              </script>
            </body>
            </html>
            """;

    public static void main(String[] args) throws IOException {
        if (args.length > 1) {
            throw new IllegalArgumentException("Usage: java audio-2d-room.java [port]");
        }
        int port = args.length == 0 ? 8080 : Integer.parseInt(args[0]);
        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535.");
        }

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", Audio2DRoom::handleRequest);
        server.start();
        System.out.println("RoomSketch is available at http://localhost:" + port);
    }

    private static void handleRequest(HttpExchange exchange) throws IOException {
        String path = exchange.getRequestURI().getPath();
        if ("/health".equals(path)) {
            send(exchange, 200, "text/plain; charset=utf-8", "ok");
        } else if ("/".equals(path) || "/index.html".equals(path)) {
            send(exchange, 200, "text/html; charset=utf-8", PAGE);
        } else {
            send(exchange, 404, "text/plain; charset=utf-8", "Not found");
        }
    }

    private static void send(HttpExchange exchange, int status, String contentType, String body) throws IOException {
        byte[] response = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        exchange.sendResponseHeaders(status, response.length);
        try (var output = exchange.getResponseBody()) {
            output.write(response);
        }
    }
}