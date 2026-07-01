import openapi from "../openapi.json" assert { type: "json" };
import { corsPreflight, json, maybeSimulateResponse, withOpenApiServer } from "../../shared/http.mjs";

const SERVICE = "users-api";
const VERSION = "1.0.0";

const users = [
  { id: "usr-001", name: "Alice Chen", email: "alice@example.com", role: "customer" },
  { id: "usr-002", name: "Bob Smith", email: "bob@example.com", role: "customer" },
  { id: "usr-003", name: "Dana Ops", email: "dana@example.com", role: "admin" },
];

export default {
  async fetch(request) {
    if (request.method === "OPTIONS") {
      return corsPreflight();
    }

    const url = new URL(request.url);
    const { pathname } = url;

    if (pathname === "/openapi.json" && request.method === "GET") {
      return json(withOpenApiServer(openapi, request.url));
    }

    const simulated = await maybeSimulateResponse(url, { service: SERVICE });
    if (simulated) {
      return simulated;
    }

    if (pathname === "/health" && request.method === "GET") {
      return json({ status: "ok", service: SERVICE, version: VERSION });
    }

    if (pathname === "/users" && request.method === "GET") {
      return json({ data: users, count: users.length });
    }

    const userMatch = pathname.match(/^\/users\/([^/]+)$/);
    if (userMatch && request.method === "GET") {
      const user = users.find((item) => item.id === userMatch[1]);
      if (!user) {
        return json({ error: "User not found" }, 404);
      }
      return json(user);
    }

    return json({ error: "Not found", service: SERVICE }, 404);
  },
};
