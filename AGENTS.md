# Agent context: postman-api-catalog-demo

GitHub: https://github.com/davidespihernandez/postman-api-catalog-demo

## Purpose

API Catalog demo: pre-deployed Cloudflare Workers (full CRUD) + Postman workspace (3 QA collections) + manual catalog integration. OpenAPI imported in Postman generates documentation collections. **No deploy during customer demo.**

## Architecture

```
./demo.sh setup → ./demo.sh setup-subdomain (once) → ./demo.sh deploy
Cloudflare: Orders, Payments, Users (*.workers.dev) — full CRUD + /openapi.json
Postman workspace → 3 QA collections + generated docs from OpenAPI import
API Catalog ← Manual import → Postman Workspace
```

## Collections (`postman/`)

Native Postman layout synced from the repo:

- `collections/Orders - QA`, `Payments - QA`, `Users - QA` — CRUD validation
- `collections/* - Doc` — documentation (includes **Payments → Refund a payment** for webhook demo)
- `environments/Production *` — `baseUrl` per API; Payments also has `refundWebhookUrl`

Worker webhook: set `REFUND_WEBHOOK_URL` in `.env` before deploy (same URL as Postman webhook).

## Commands

`setup`, `setup-subdomain` (one-time interactive workers.dev registration), `deploy`, `reset` (= deploy all), `add`, `smoke`, `urls`

**Setup order:** `setup` → `setup-subdomain` → `deploy`. Deploy alone fails on new Cloudflare accounts without a workers.dev subdomain.

## Demo (Postman only)

1. Catalog portfolio — 3 integrated services  
2. Service deep-dive — Overview / Development / Test  
3. Run *QA collection (CRUD flow)  
4. Test tab — metrics from QA run  

See `DEMO-STEPS.md`.

## Workers

`postman-api-catalog-demo-orders`, `-payments`, `-users`

OpenAPI source of truth: repo-root `orders.yaml`, `payments.yaml`, `users.yaml` (Postman sync). Deploy copies each to `apis/<api>/openapi.json` before `wrangler deploy`.

Each supports: GET list, POST create, GET/PUT/PATCH/DELETE by id. Payments also has POST `/payments/refund` (POSTs webhook payload to `REFUND_WEBHOOK_URL` worker var, set from `.env` at deploy).
