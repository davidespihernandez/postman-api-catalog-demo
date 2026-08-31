// Orders API — plain Node (Express). In-memory CRUD; resets to seed data on restart.
import express from "express";
import { readFile } from "node:fs/promises";
import { randomUUID } from "node:crypto";
import { cors, demoHooks, openApiWithServer } from "../../shared/app.mjs";

const SERVICE = "orders-api";
const VERSION = "1.0.0";
const PORT = Number(process.env.PORT) || 8787;
const openapi = JSON.parse(await readFile(new URL("../openapi.json", import.meta.url)));

let orders = [
  { id: "ord-001", customerId: "usr-001", status: "shipped", total: 49.99, currency: "USD" },
  { id: "ord-002", customerId: "usr-002", status: "pending", total: 129.5, currency: "USD" },
];
const nextId = () => `ord-${randomUUID().slice(0, 8)}`;
const findIndex = (id) => orders.findIndex((o) => o.id === id);

const app = express();
app.set("trust proxy", true);   // honor X-Forwarded-Proto/Host from Caddy
app.set("json spaces", 2);
app.use(cors);
app.use(express.json());
app.use(demoHooks);

app.get("/openapi.json", (req, res) => res.json(openApiWithServer(openapi, req)));
app.get("/health", (req, res) => res.json({ status: "ok", service: SERVICE, version: VERSION }));

app.get("/orders", (req, res) => res.json({ data: orders, count: orders.length }));

app.post("/orders", (req, res) => {
  const b = req.body;
  if (!b || typeof b.customerId !== "string" || typeof b.total !== "number") {
    return res.status(400).json({ error: "customerId (string) and total (number) are required" });
  }
  const order = {
    id: nextId(),
    customerId: b.customerId,
    status: b.status ?? "pending",
    total: b.total,
    currency: b.currency ?? "USD",
  };
  orders.push(order);
  res.status(201).json(order);
});

app.get("/orders/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Order not found" });
  res.json(orders[i]);
});

app.put("/orders/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Order not found" });
  const b = req.body;
  if (!b || typeof b.customerId !== "string" || typeof b.total !== "number" || typeof b.status !== "string") {
    return res.status(400).json({ error: "customerId, total, and status are required" });
  }
  orders[i] = {
    id: req.params.id,
    customerId: b.customerId,
    status: b.status,
    total: b.total,
    currency: b.currency ?? orders[i].currency,
  };
  res.json(orders[i]);
});

app.patch("/orders/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Order not found" });
  if (!req.body || typeof req.body !== "object") return res.status(400).json({ error: "Request body required" });
  orders[i] = { ...orders[i], ...req.body, id: req.params.id };
  res.json(orders[i]);
});

app.delete("/orders/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "Order not found" });
  orders.splice(i, 1);
  res.status(204).end();
});

app.use((req, res) => res.status(404).json({ error: "Not found", service: SERVICE }));

app.listen(PORT, "127.0.0.1", () => console.log(`${SERVICE} on http://127.0.0.1:${PORT}`));
