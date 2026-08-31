// Shared helpers for the three plain-Node (Express) API services.
import { setTimeout as sleep } from "node:timers/promises";

// Permissive CORS for the browser frontend + Postman; answers preflight directly.
export function cors(req, res, next) {
  res.set({
    "access-control-allow-origin": "*",
    "access-control-allow-methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
    "access-control-allow-headers": "content-type, authorization, x-api-key",
  });
  if (req.method === "OPTIONS") return res.status(204).end();
  next();
}

const MAX_DEMO_DELAY_MS = 30_000;

// Optional demo hooks on any endpoint except /openapi.json:
//   ?delay=2000   wait N ms before responding (max 30000)
//   ?status=500   return that HTTP status with a simulated error body (?error= is an alias)
export async function demoHooks(req, res, next) {
  if (req.path === "/openapi.json") return next();
  const q = req.query;

  const delayRaw = q.delay ?? q.wait;
  let delayMs = 0;
  if (delayRaw != null && delayRaw !== "") {
    const n = Number(delayRaw);
    if (Number.isFinite(n) && n > 0) delayMs = Math.min(Math.floor(n), MAX_DEMO_DELAY_MS);
  }

  const statusRaw = q.status ?? q.error;
  let forced = null;
  if (statusRaw != null && statusRaw !== "") {
    const n = Number(statusRaw);
    if (Number.isFinite(n) && n >= 100 && n <= 599) forced = Math.floor(n);
    else if (statusRaw === "true") forced = 500;
  }

  if (delayMs > 0) await sleep(delayMs);
  if (forced != null) {
    return res.status(forced).json({
      error: "Simulated response for API Catalog demo",
      status: forced,
      simulated: true,
      ...(delayMs > 0 ? { delayMs } : {}),
    });
  }
  next();
}

// Return the OpenAPI spec with its `servers` set to the public request origin.
export function openApiWithServer(openapi, req) {
  return {
    ...structuredClone(openapi),
    servers: [{ url: `${req.protocol}://${req.get("host")}`, description: "Deployed API" }],
  };
}

// POST a JSON payload to a webhook URL (no-op when url is empty). Returns true on 2xx.
export async function postWebhook(url, payload) {
  if (!url) return false;
  try {
    const r = await fetch(url, {
      method: "POST",
      headers: { "content-type": "application/json" },
      body: JSON.stringify(payload),
    });
    return r.ok;
  } catch {
    return false;
  }
}
