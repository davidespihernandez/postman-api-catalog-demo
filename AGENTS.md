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

- `orders-qa`, `payments-qa`, `users-qa` — CRUD validation with chained test scripts
- `demo.environment.json`

Docs collections: import `/openapi.json` per API in Postman (not in repo).

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

Each supports: GET list, POST create, GET/PUT/PATCH/DELETE by id. Payments also has POST `/payments/refund`.
