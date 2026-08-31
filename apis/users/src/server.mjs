// Users API — plain Node (Express). In-memory CRUD; resets to seed data on restart.
import express from "express";
import { readFile } from "node:fs/promises";
import { randomUUID } from "node:crypto";
import { cors, demoHooks, openApiWithServer } from "../../shared/app.mjs";

const SERVICE = "users-api";
const VERSION = "1.0.0";
const PORT = Number(process.env.PORT) || 8789;
const openapi = JSON.parse(await readFile(new URL("../openapi.json", import.meta.url)));

let users = [
  { id: "usr-001", name: "Alice Chen", email: "alice@example.com", role: "customer" },
  { id: "usr-002", name: "Bob Smith", email: "bob@example.com", role: "customer" },
  { id: "usr-003", name: "Dana Ops", email: "dana@example.com", role: "admin" },
];
const nextId = () => `usr-${randomUUID().slice(0, 8)}`;
const findIndex = (id) => users.findIndex((u) => u.id === id);

const app = express();
app.set("trust proxy", true);
app.set("json spaces", 2);
app.use(cors);
app.use(express.json());
app.use(demoHooks);

app.get("/openapi.json", (req, res) => res.json(openApiWithServer(openapi, req)));
app.get("/health", (req, res) => res.json({ status: "ok", service: SERVICE, version: VERSION }));

app.get("/users", (req, res) => res.json({ data: users, count: users.length }));

app.post("/users", (req, res) => {
  const b = req.body;
  if (!b || typeof b.name !== "string" || typeof b.email !== "string") {
    return res.status(400).json({ error: "name (string) and email (string) are required" });
  }
  const user = { id: nextId(), name: b.name, email: b.email, role: b.role ?? "customer" };
  users.push(user);
  res.status(201).json(user);
});

app.get("/users/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "User not found" });
  res.json(users[i]);
});

app.put("/users/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "User not found" });
  const b = req.body;
  if (!b || typeof b.name !== "string" || typeof b.email !== "string" || typeof b.role !== "string") {
    return res.status(400).json({ error: "name, email, and role are required" });
  }
  users[i] = { id: req.params.id, name: b.name, email: b.email, role: b.role };
  res.json(users[i]);
});

app.patch("/users/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "User not found" });
  if (!req.body || typeof req.body !== "object") return res.status(400).json({ error: "Request body required" });
  users[i] = { ...users[i], ...req.body, id: req.params.id };
  res.json(users[i]);
});

app.delete("/users/:id", (req, res) => {
  const i = findIndex(req.params.id);
  if (i === -1) return res.status(404).json({ error: "User not found" });
  users.splice(i, 1);
  res.status(204).end();
});

app.use((req, res) => res.status(404).json({ error: "Not found", service: SERVICE }));

app.listen(PORT, "127.0.0.1", () => console.log(`${SERVICE} on http://127.0.0.1:${PORT}`));
