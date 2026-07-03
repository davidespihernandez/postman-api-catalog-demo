import openapi from "../openapi.json" assert { type: "json" };
import {
  corsPreflight,
  json,
  maybeSimulateResponse,
  readJson,
  withOpenApiServer,
} from "../../shared/http.mjs";

const SERVICE = "orders-api";
const VERSION = "1.0.0";

const seedOrders = () => [
  { id: "ord-001", customerId: "usr-001", status: "shipped", total: 49.99, currency: "USD" },
  { id: "ord-002", customerId: "usr-002", status: "pending", total: 129.5, currency: "USD" },
];

let orders = seedOrders();

function nextId(prefix) {
  const suffix = crypto.randomUUID().slice(0, 8);
  return `${prefix}-${suffix}`;
}

function findOrderIndex(id) {
  return orders.findIndex((item) => item.id === id);
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

    if (pathname === "/orders" && request.method === "GET") {
      return json({ data: orders, count: orders.length });
    }

    if (pathname === "/orders" && request.method === "POST") {
      const body = await readJson(request);
      if (!body || typeof body.customerId !== "string" || typeof body.total !== "number") {
        return json({ error: "customerId (string) and total (number) are required" }, 400);
      }

      const order = {
        id: nextId("ord"),
        customerId: body.customerId,
        status: body.status ?? "pending",
        total: body.total,
        currency: body.currency ?? "USD",
      };
      orders.push(order);
      return json(order, 201);
    }

    const orderMatch = pathname.match(/^\/orders\/([^/]+)$/);
    if (orderMatch) {
      const orderId = orderMatch[1];
      const index = findOrderIndex(orderId);

      if (request.method === "GET") {
        if (index === -1) {
          return json({ error: "Order not found" }, 404);
        }
        return json(orders[index]);
      }

      if (request.method === "PUT") {
        if (index === -1) {
          return json({ error: "Order not found" }, 404);
        }
        const body = await readJson(request);
        if (
          !body ||
          typeof body.customerId !== "string" ||
          typeof body.total !== "number" ||
          typeof body.status !== "string"
        ) {
          return json({ error: "customerId, total, and status are required" }, 400);
        }
        orders[index] = {
          id: orderId,
          customerId: body.customerId,
          status: body.status,
          total: body.total,
          currency: body.currency ?? orders[index].currency,
        };
        return json(orders[index]);
      }

      if (request.method === "PATCH") {
        if (index === -1) {
          return json({ error: "Order not found" }, 404);
        }
        const body = await readJson(request);
        if (!body || typeof body !== "object") {
          return json({ error: "Request body required" }, 400);
        }
        orders[index] = { ...orders[index], ...body, id: orderId };
        return json(orders[index]);
      }

      if (request.method === "DELETE") {
        if (index === -1) {
          return json({ error: "Order not found" }, 404);
        }
        orders.splice(index, 1);
        return new Response(null, { status: 204 });
      }
    }

    return json({ error: "Not found", service: SERVICE }, 404);
  },
};
