# Agent context: postman-api-catalog-demo

GitHub: https://github.com/davidespihernandez/postman-api-catalog-demo

## Purpose

API Catalog demo: pre-deployed Cloudflare Workers (full CRUD) + Postman workspace (3 QA collections) + manual catalog integration. OpenAPI imported in Postman generates documentation collections. **No deploy during customer demo.**

## Architecture

```
./demo.sh setup → ./demo.sh setup-subdomain (once) → ./demo.sh deploy
Cloudflare: Orders, Payments, Users (*.workers.dev) — full CRUD + /openapi.json
Async: MQTT broker (HiveMQ public) — Postman MQTT request publishes directly to topic
Optional: ./demo.sh mqtt-bridge → NOTIFICATION_WEBHOOK_URL
Postman workspace → QA collections + OpenAPI/AsyncAPI specs
API Catalog ← Manual import → Postman Workspace
```

## Collections (`postman/`)

Native Postman layout synced from the repo:

- `collections/Orders - QA`, `Payments - QA`, `Users - QA` — CRUD validation
- `collections/* - Doc` — documentation (includes **Payments → Refund a payment** for webhook demo)
- `environments/Production *` — `baseUrl` per API; Notifications uses `mqttBrokerUrl` + `mqttTopic`

Webhooks: `REFUND_WEBHOOK_URL` in `.env` before deploy; `NOTIFICATION_WEBHOOK_URL` for optional mqtt-bridge.

## Commands

`setup`, `setup-subdomain`, `deploy`, `reset`, `add`, `smoke`, `urls`, `mqtt-bridge`

**Setup order:** `setup` → `setup-subdomain` → `deploy`. Deploy alone fails on new Cloudflare accounts without a workers.dev subdomain.

## Demo (Postman only)

1. Catalog portfolio — integrated services  
2. Service deep-dive — Overview / Development / Test  
3. Run *QA collection (CRUD flow)  
4. Async: MQTT publish to broker (separate from REST refund webhook demo)  

See `DEMO-STEPS.md` and `SE-INSTALL.md`.

## Workers

`postman-api-catalog-demo-orders`, `-payments`, `-users`

OpenAPI: `orders.yaml`, `payments.yaml`, `users.yaml`, `payment-refund-webhook.yaml`. AsyncAPI: `notifications.asyncapi.yaml` (MQTT — no worker). Deploy copies REST specs to `apis/<api>/openapi.json`.

Payments: `POST /payments/refund` → `REFUND_WEBHOOK_URL`. Notifications: Postman **MQTT** → broker topic; optional `mqtt-bridge` → `NOTIFICATION_WEBHOOK_URL`.
