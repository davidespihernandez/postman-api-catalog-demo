import openapi from "../openapi.json" assert { type: "json" };
import {
  corsPreflight,
  json,
  maybeSimulateResponse,
  readJson,
  withOpenApiServer,
} from "../../shared/http.mjs";

const SERVICE = "users-api";
const VERSION = "1.0.0";

const seedUsers = () => [
  { id: "usr-001", name: "Alice Chen", email: "alice@example.com", role: "customer" },
  { id: "usr-002", name: "Bob Smith", email: "bob@example.com", role: "customer" },
  { id: "usr-003", name: "Dana Ops", email: "dana@example.com", role: "admin" },
];

let users = seedUsers();

function nextId(prefix) {
  const suffix = crypto.randomUUID().slice(0, 8);
  return `${prefix}-${suffix}`;
}

function findUserIndex(id) {
  return users.findIndex((item) => item.id === id);
}

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

    if (pathname === "/users" && request.method === "POST") {
      const body = await readJson(request);
      if (!body || typeof body.name !== "string" || typeof body.email !== "string") {
        return json({ error: "name (string) and email (string) are required" }, 400);
      }

      const user = {
        id: nextId("usr"),
        name: body.name,
        email: body.email,
        role: body.role ?? "customer",
      };
      users.push(user);
      return json(user, 201);
    }

    const userMatch = pathname.match(/^\/users\/([^/]+)$/);
    if (userMatch) {
      const userId = userMatch[1];
      const index = findUserIndex(userId);

      if (request.method === "GET") {
        if (index === -1) {
          return json({ error: "User not found" }, 404);
        }
        return json(users[index]);
      }

      if (request.method === "PUT") {
        if (index === -1) {
          return json({ error: "User not found" }, 404);
        }
        const body = await readJson(request);
        if (
          !body ||
          typeof body.name !== "string" ||
          typeof body.email !== "string" ||
          typeof body.role !== "string"
        ) {
          return json({ error: "name, email, and role are required" }, 400);
        }
        users[index] = {
          id: userId,
          name: body.name,
          email: body.email,
          role: body.role,
        };
        return json(users[index]);
      }

      if (request.method === "PATCH") {
        if (index === -1) {
          return json({ error: "User not found" }, 404);
        }
        const body = await readJson(request);
        if (!body || typeof body !== "object") {
          return json({ error: "Request body required" }, 400);
        }
        users[index] = { ...users[index], ...body, id: userId };
        return json(users[index]);
      }

      if (request.method === "DELETE") {
        if (index === -1) {
          return json({ error: "User not found" }, 404);
        }
        users.splice(index, 1);
        return new Response(null, { status: 204 });
      }
    }

    return json({ error: "Not found", service: SERVICE }, 404);
  },
};
