#!/usr/bin/env node

import { readFileSync, existsSync } from "node:fs";
import { resolve, dirname } from "node:path";
import { fileURLToPath } from "node:url";

const __dirname = dirname(fileURLToPath(import.meta.url));
const root = resolve(__dirname, "..");

function loadEnvFile(path) {
  if (!existsSync(path)) return {};
  const out = {};
  for (const line of readFileSync(path, "utf8").split("\n")) {
    const trimmed = line.trim();
    if (!trimmed || trimmed.startsWith("#")) continue;
    const idx = trimmed.indexOf("=");
    if (idx === -1) continue;
    out[trimmed.slice(0, idx)] = trimmed.slice(idx + 1);
  }
  return out;
}

const env = {
  ...loadEnvFile(resolve(root, ".env")),
  ...loadEnvFile(resolve(root, ".demo-state.env")),
  ...process.env,
};

const api = process.argv[2];
if (!api) {
  console.error("Usage: register-api.mjs <orders|payments|users>");
  process.exit(1);
}

const apiKey = env.POSTMAN_API_KEY;
if (!apiKey) {
  console.error("POSTMAN_API_KEY is required in .env");
  process.exit(1);
}

const urlKey = `${api.toUpperCase()}_API_URL`;
const baseUrl = env[urlKey];
if (!baseUrl) {
  console.error(`${urlKey} is required. Deploy the API first.`);
  process.exit(1);
}

const titles = {
  orders: "Orders API",
  payments: "Payments API",
  users: "Users API",
};

const title = titles[api];
if (!title) {
  console.error(`Unknown API: ${api}`);
  process.exit(1);
}

const specRes = await fetch(`${baseUrl}/openapi.json`);
if (!specRes.ok) {
  console.error(`Failed to fetch OpenAPI from ${baseUrl}/openapi.json (${specRes.status})`);
  process.exit(1);
}

const spec = await specRes.json();
const encoded = Buffer.from(JSON.stringify(spec)).toString("base64");

const listRes = await fetch("https://api.getpostman.com/api-catalog/discovery-services?limit=100", {
  headers: { "X-Api-Key": apiKey },
});

if (!listRes.ok) {
  const body = await listRes.text();
  console.error(`List discovery services failed (${listRes.status}): ${body}`);
  process.exit(1);
}

const listed = await listRes.json();
const existing = (listed.data ?? []).find((item) =>
  (item.name ?? "").toLowerCase().includes(api),
);

if (existing?.id) {
  console.log(`Already discovered: ${existing.name} (${existing.id})`);
  console.log("Open API Catalog → Service discovery and integrate this service if needed.");
  process.exit(0);
}

const payload = {
  discoveredServices: [
    {
      name: title,
      version: spec.info?.version ?? "1.0.0",
      description: spec.info?.description ?? `${title} demo backend`,
      sourceEnvironment: env.DEMO_ENVIRONMENT ?? "demo",
      apiDefinition: { content: encoded },
      tags: ["catalog-demo", api],
    },
  ],
};

const createRes = await fetch("https://api.getpostman.com/api-catalog/discovery-services", {
  method: "POST",
  headers: {
    "X-Api-Key": apiKey,
    "Content-Type": "application/json",
  },
  body: JSON.stringify(payload),
});

const createBody = await createRes.text();
if (!createRes.ok) {
  console.error(`Register API failed (${createRes.status}): ${createBody}`);
  console.error(
    "\nEnsure your team has Enterprise API Catalog access. You can also integrate manually in API Catalog → Service discovery.",
  );
  process.exit(1);
}

console.log(createBody);
console.log(`\nRegistered ${title}. Open API Catalog → Service discovery to integrate it.`);
