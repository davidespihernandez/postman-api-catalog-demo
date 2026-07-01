export function json(data, status = 200, extraHeaders = {}) {
  return new Response(JSON.stringify(data, null, 2), {
    status,
    headers: {
      "content-type": "application/json; charset=utf-8",
      "access-control-allow-origin": "*",
      "access-control-allow-methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
      "access-control-allow-headers": "content-type, authorization, x-api-key",
      ...extraHeaders,
    },
  });
}

export function noContent() {
  return new Response(null, {
    status: 204,
    headers: {
      "access-control-allow-origin": "*",
    },
  });
}

export function corsPreflight() {
  return new Response(null, {
    status: 204,
    headers: {
      "access-control-allow-origin": "*",
      "access-control-allow-methods": "GET, POST, PUT, PATCH, DELETE, OPTIONS",
      "access-control-allow-headers": "content-type, authorization, x-api-key",
    },
  });
}

export async function readJson(request) {
  try {
    return await request.json();
  } catch {
    return null;
  }
}

export function withOpenApiServer(openapi, requestUrl) {
  const spec = structuredClone(openapi);
  const origin = new URL(requestUrl).origin;
  spec.servers = [{ url: origin, description: "Deployed worker" }];
  return spec;
}

const MAX_DEMO_DELAY_MS = 30_000;

/** @param {URL} url */
export function parseDemoQuery(url) {
  const delayRaw = url.searchParams.get("delay") ?? url.searchParams.get("wait");
  let delayMs = 0;
  if (delayRaw != null && delayRaw !== "") {
    const parsed = Number(delayRaw);
    if (Number.isFinite(parsed) && parsed > 0) {
      delayMs = Math.min(Math.floor(parsed), MAX_DEMO_DELAY_MS);
    }
  }

  const statusRaw = url.searchParams.get("status") ?? url.searchParams.get("error");
  let forcedStatus = null;
  if (statusRaw != null && statusRaw !== "") {
    const parsed = Number(statusRaw);
    if (Number.isFinite(parsed) && parsed >= 100 && parsed <= 599) {
      forcedStatus = Math.floor(parsed);
    } else if (statusRaw === "true") {
      forcedStatus = 500;
    }
  }

  return { delayMs, forcedStatus };
}

/**
 * Optional demo query params on any endpoint except /openapi.json:
 *   ?delay=2000       wait N ms before responding (max 30000)
 *   ?status=500       return HTTP status N with a simulated error body
 *   ?error=500        alias for status
 */
export async function maybeSimulateResponse(url, extra = {}) {
  const { delayMs, forcedStatus } = parseDemoQuery(url);
  if (delayMs > 0) {
    await new Promise((resolve) => setTimeout(resolve, delayMs));
  }
  if (forcedStatus != null) {
    return json(
      {
        error: "Simulated response for API Catalog demo",
        status: forcedStatus,
        simulated: true,
        ...(delayMs > 0 ? { delayMs } : {}),
        ...extra,
      },
      forcedStatus,
    );
  }
  return null;
}
