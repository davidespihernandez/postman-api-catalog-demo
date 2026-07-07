import openapi from "../openapi.json" assert { type: "json" };
import {
  corsPreflight,
  json,
  maybeSimulateResponse,
  postWebhook,
  readJson,
  withOpenApiServer,
} from "../../shared/http.mjs";

const SERVICE = "payments-api";
const VERSION = "1.0.0";

const seedPayments = () => [
  { id: "pay-001", orderId: "ord-001", amount: 49.99, status: "completed", currency: "USD" },
];

let payments = seedPayments();

function nextId(prefix) {
  const suffix = crypto.randomUUID().slice(0, 8);
  return `${prefix}-${suffix}`;
}

function findPaymentIndex(id) {
  return payments.findIndex((item) => item.id === id);
}

export default {
  async fetch(request, env) {
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

    if (pathname === "/payments" && request.method === "GET") {
      return json({ data: payments, count: payments.length });
    }

    if (pathname === "/payments" && request.method === "POST") {
      const body = await readJson(request);
      if (!body || typeof body.orderId !== "string" || typeof body.amount !== "number") {
        return json({ error: "orderId (string) and amount (number) are required" }, 400);
      }

      const payment = {
        id: nextId("pay"),
        orderId: body.orderId,
        amount: body.amount,
        status: body.status ?? "completed",
        currency: body.currency ?? "USD",
      };
      payments.push(payment);
      return json(payment, 201);
    }

    if (pathname === "/payments/refund" && request.method === "POST") {
      const body = await readJson(request);
      if (!body || typeof body.paymentId !== "string") {
        return json({ error: "paymentId (string) is required" }, 400);
      }

      const payment = payments.find((item) => item.id === body.paymentId);
      if (!payment) {
        return json({ error: "Payment not found" }, 404);
      }

      payment.status = "refunded";
      payment.refundReason = body.reason ?? "customer_request";
      payment.refundedAt = new Date().toISOString();

      const webhookPayload = {
        event: "payment.refunded",
        occurredAt: payment.refundedAt,
        payment: {
          id: payment.id,
          orderId: payment.orderId,
          amount: payment.amount,
          status: payment.status,
          currency: payment.currency,
          refundReason: payment.refundReason,
          refundedAt: payment.refundedAt,
        },
      };
      await postWebhook(env.REFUND_WEBHOOK_URL, webhookPayload);

      return json(payment);
    }

    const paymentMatch = pathname.match(/^\/payments\/([^/]+)$/);
    if (paymentMatch) {
      const paymentId = paymentMatch[1];
      const index = findPaymentIndex(paymentId);

      if (request.method === "GET") {
        if (index === -1) {
          return json({ error: "Payment not found" }, 404);
        }
        return json(payments[index]);
      }

      if (request.method === "PUT") {
        if (index === -1) {
          return json({ error: "Payment not found" }, 404);
        }
        const body = await readJson(request);
        if (
          !body ||
          typeof body.orderId !== "string" ||
          typeof body.amount !== "number" ||
          typeof body.status !== "string"
        ) {
          return json({ error: "orderId, amount, and status are required" }, 400);
        }
        payments[index] = {
          id: paymentId,
          orderId: body.orderId,
          amount: body.amount,
          status: body.status,
          currency: body.currency ?? payments[index].currency,
        };
        return json(payments[index]);
      }

      if (request.method === "PATCH") {
        if (index === -1) {
          return json({ error: "Payment not found" }, 404);
        }
        const body = await readJson(request);
        if (!body || typeof body !== "object") {
          return json({ error: "Request body required" }, 400);
        }
        payments[index] = { ...payments[index], ...body, id: paymentId };
        return json(payments[index]);
      }

      if (request.method === "DELETE") {
        if (index === -1) {
          return json({ error: "Payment not found" }, 404);
        }
        payments.splice(index, 1);
        return new Response(null, { status: 204 });
      }
    }

    return json({ error: "Not found", service: SERVICE }, 404);
  },
};
