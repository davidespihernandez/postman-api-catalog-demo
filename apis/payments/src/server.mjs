// Payments API — plain Node (Express). In-memory CRUD + refund webhook.
import express from "express";
import { readFile } from "node:fs/promises";
import { randomUUID } from "node:crypto";
import { cors, demoHooks, openApiWithServer, postWebhook } from "../../shared/app.mjs";

const SERVICE = "payments-api";
const VERSION = "1.0.0";
const PORT = Number(process.env.PORT) || 8788;
const REFUND_WEBHOOK_URL = process.env.REFUND_WEBHOOK_URL || "";
const openapi = JSON.parse(await readFile(new URL("../openapi.json", import.meta.url)));

let payments = [
  { id: "pay-001", orderId: "ord-001", amount: 49.99, status: "completed", currency: "USD" },
];
const nextId = () => `pay-${randomUUID().slice(0, 8)}`;
const findIndex = (id) => payments.findIndex((p) => p.id === id);

const app = express();
app.set("trust proxy", true);
app.set("json spaces", 2);
app.use(cors);
app.use(express.json());
app.use(demoHooks);

app.get("/openapi.json", (req, res) => res.json(openApiWithServer(openapi, req)));
app.get("/health", (req, res) => res.json({ status: "ok", service: SERVICE, version: VERSION }));

app.get("/payments", (req, res) => res.json({ data: payments, count: payments.length }));

app.post("/payments", (req, res) => {
  const b = req.body;
  if (!b || typeof b.orderId !== "string" || typeof b.amount !== "number") {
    return res.status(400).json({ error: "orderId (string) and amount (number) are required" });
  }
  const payment = {
    id: nextId(),
    orderId: b.orderId,
    amount: b.amount,
    status: b.status ?? "completed",
    currency: b.currency ?? "USD",
  };
  payments.push(payment);
  res.status(201).json(payment);
});

// Registered before /payments/:id so "refund" isn't treated as an id.
app.post("/payments/refund", async (req, res) => {
  const b = req.body;
  if (!b || typeof b.paymentId !== "string") {
    return res.status(400).json({ error: "paymentId (string) is required" });
  }
  const payment = payments.find((p) => p.id === b.paymentId);
  if (!payment) return res.status(404).json({ error: "Payment not found" });

  payment.status = "refunded";
  payment.refundReason = b.reason ?? "customer_request";
  payment.refundedAt = new Date().toISOString();

  await postWebhook(REFUND_WEBHOOK_URL, {
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
  });

  res.json(payment);
});

app.get("/payments/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Payment not found" });
  res.json(payments[i]);
});

app.put("/payments/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Payment not found" });
  const b = req.body;
  if (!b || typeof b.orderId !== "string" || typeof b.amount !== "number" || typeof b.status !== "string") {
    return res.status(400).json({ error: "orderId, amount, and status are required" });
  }
  payments[i] = {
    id: req.params.id,
    orderId: b.orderId,
    amount: b.amount,
    status: b.status,
    currency: b.currency ?? payments[i].currency,
  };
  res.json(payments[i]);
});

app.patch("/payments/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Payment not found" });
  if (!req.body || typeof req.body !== "object") return res.status(400).json({ error: "Request body required" });
  payments[i] = { ...payments[i], ...req.body, id: req.params.id };
  res.json(payments[i]);
});

app.delete("/payments/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Payment not found" });
  payments.splice(i, 1);
  res.status(204).end();
});

app.use((req, res) => res.status(404).json({ error: "Not found", service: SERVICE }));

app.listen(PORT, "127.0.0.1", () => console.log(`${SERVICE} on http://127.0.0.1:${PORT}`));
