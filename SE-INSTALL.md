# SE installation guide — API Catalog demo

One-time setup for Solutions Engineers. **Do not deploy during the customer demo** — complete this guide beforehand.

Repository: [github.com/davidespihernandez/postman-api-catalog-demo](https://github.com/davidespihernandez/postman-api-catalog-demo)

Presenter script: [DEMO-STEPS.md](DEMO-STEPS.md)

---

## Prerequisites

| Requirement | Notes |
|-------------|--------|
| **Postman Enterprise** with **API Catalog** | Required |
| **Team Admin** | Connect catalog sources |
| **Cloudflare account** | Free tier |
| **Node.js 18+** and **curl** | For `./demo.sh` |

---

## Part 1 — Deploy all APIs (terminal, before the demo)

### 1. Clone and authenticate

```bash
git clone https://github.com/davidespihernandez/postman-api-catalog-demo.git
cd postman-api-catalog-demo
./demo.sh setup
```

This installs dependencies and logs in to Cloudflare (browser OAuth, or `CLOUDFLARE_API_TOKEN` in `.env` — see `.env.example`).

### 2. Register your workers.dev subdomain (one-time)

```bash
./demo.sh setup-subdomain
```

**Run this in an interactive terminal** — not via CI or a script that captures output.

Wrangler asks whether to register a `*.workers.dev` subdomain. Answer **yes** and pick a name (e.g. `acme-demo` → workers at `https://<worker>.acme-demo.workers.dev`).

This step deploys the Orders worker once to complete Cloudflare onboarding. `./demo.sh deploy` cannot do this step because it runs wrangler non-interactively.

If you already have a subdomain on this Cloudflare account, wrangler skips registration and deploys Orders normally.

Cloudflare may take **a few minutes** to propagate DNS/SSL for a new subdomain. If `./demo.sh deploy` fails on the health check with an SSL error, wait 2–3 minutes and run `./demo.sh deploy` again (or `./demo.sh smoke`).

**Dashboard alternative:** [Workers & Pages](https://dash.cloudflare.com/?to=/:account/workers-and-pages) → change **Your subdomain**.

### 3. Deploy all APIs

```bash
./demo.sh deploy
./demo.sh urls
```

`deploy` publishes **Orders**, **Payments**, and **Users** to Cloudflare and runs health checks. Paste the URLs from `./demo.sh urls` into the Postman environment (Part 2).

If worker URLs change after redeploy, update the Postman environment.

---

## Part 2 — Postman workspace

Create team workspace **API Catalog Demo**.

### Postman workspace layout

Sync or import from the `postman/` folder:

```
postman/
├── collections/          Orders - QA, Payments - QA, Users - QA, * - Doc, …
├── environments/         Production Orders / Payments / Users / Notifications (+ Mock)
└── globals/
```

Use **Production** environments — each sets `baseUrl` to the matching worker URL from `./demo.sh urls`.

### OpenAPI specs (repo root)

| Spec | Role |
|------|------|
| `orders.yaml`, `payments.yaml`, `users.yaml` | Backend REST APIs (Cloudflare Workers) |
| `payment-refund-webhook.yaml` | **Inbound** refund notification contract (Postman webhook — not part of Payments backend) |
| `notifications.asyncapi.yaml` | **Async** MQTT notification contract (publish directly from Postman MQTT request) |

Import or sync all specs in the workspace.

### Configure webhooks in `.env`

**Payments refund** — worker POSTs after `POST /payments/refund`:

```bash
REFUND_WEBHOOK_URL=https://7ygtn6bgtcbmv8k2oigij4xc0.webhook.pstmn.io/
```

Redeploy Payments (or run full `./demo.sh deploy`) so the worker picks up `REFUND_WEBHOOK_URL`.

**Notifications async** — optional mqtt-bridge forwards processed messages to a second Postman webhook:

```bash
NOTIFICATION_WEBHOOK_URL=https://YOUR-NOTIFICATION-WEBHOOK.webhook.pstmn.io/
MQTT_BROKER_URL=mqtt://broker.hivemq.com:1883
MQTT_TOPIC=postman-api-catalog-demo/notifications
```

### Async notifications demo (MQTT request — not HTTP)

This is the **direct-to-broker** demo. Use Postman's **MQTT** request type (New → MQTT), not a REST request.

1. Import `notifications.asyncapi.yaml` into Spec Hub.
2. Create a manual **MQTT** collection request (save to your workspace).
3. Use **Production Notifications** environment (`mqttBrokerUrl`, `mqttTopic`).

| Step | Action |
|------|--------|
| Connect | `{{mqttBrokerUrl}}` → **Connect** |
| Subscribe | Topic tab → add `{{mqttTopic}}` |
| Publish | Message tab → topic `{{mqttTopic}}` → JSON payload (see AsyncAPI spec) → **Send** |

Example payload:

```json
{
  "type": "notification",
  "id": "notif-demo-001",
  "message": "Your order has shipped",
  "channel": "email",
  "recipient": "alice@example.com",
  "publishedAt": "2026-07-10T10:00:00.000Z"
}
```

You should see the message in the response stream (outgoing ↑ and incoming ↓) — proof it reached the broker.

**Webhook proof (optional):** before the demo, in a separate terminal run `./demo.sh mqtt-bridge` (subscribes to the topic and POSTs `notification.processed` to your webhook). Keep it running during the presentation.

### Configure refund webhook on the Payments worker

(`refundWebhookUrl` is in **Production Payments** environment for reference.)

### Collections in this repo

| Collection | Purpose |
|------------|---------|
| `collections/Orders - QA` | Orders CRUD validation (7 requests, chained tests) |
| `collections/Payments - QA` | Payments CRUD validation |
| `collections/Users - QA` | Users CRUD validation |
| `collections/Payments - Doc` | Payments docs; **Refund a payment** triggers outbound webhook |
| `collections/Refund Webhook - Doc` | Inbound webhook contract (what Payments publishes) |
| `collections/Orders - Doc`, `Users - Doc` | Generated documentation from OpenAPI |

QA collections run in order: **Create → Get → List → PUT → PATCH → Delete → Get (404)**.

### Verify before the demo

- [ ] Production environment `baseUrl` values match `./demo.sh urls`
- [ ] Run each **QA** collection — all green
- [ ] Run **Payments - Doc → Refund a payment** — check Postman webhook receives payload matching **Refund Webhook - Doc** spec
- [ ] Run your **MQTT** notification request — message appears in subscribe stream
- [ ] (Optional) With `./demo.sh mqtt-bridge` running, webhook receives `notification.processed`

---

## Part 3 — API Catalog

### 1. Link workspace

1. **Home → API Catalog → Service discovery → Add New Sources**
2. **Manual import → Postman Workspace**
3. Select **API Catalog Demo**

Docs: [Connect API Catalog to your services](https://learning.postman.com/docs/api-catalog/connect/overview)

### 2. Integrate all three services

For **Orders API**, **Payments API**, **Users API**, and **Notifications API** (AsyncAPI / MQTT):

1. **Service discovery** → select service
2. **Integrate** → **Test** system environment
3. Confirm each appears under **Integrated services**

Tip: collection names (`Orders API — QA`, etc.) match service names so the catalog can tie tests to the right API.

### 3. Link Test environment

- [ ] Connect the Postman environment and collection runs to the catalog **Test** system environment (so runs appear on the **Test** tab)

Docs: [Explore the API Catalog](https://learning.postman.com/docs/api-catalog/explore)

---

## Part 4 — Pre-demo checklist

- [ ] `./demo.sh setup` and `./demo.sh setup-subdomain` completed (one-time)
- [ ] `./demo.sh smoke` → all APIs OK
- [ ] Postman environment URLs match `./demo.sh urls`
- [ ] Three services in **Integrated services**
- [ ] All three QA collections run successfully
- [ ] Read [DEMO-STEPS.md](DEMO-STEPS.md)

---

## Reset between meetings

```bash
./demo.sh deploy
./demo.sh urls
```

Update Postman environment if URLs changed.

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `register a workers.dev subdomain` | Run `./demo.sh setup-subdomain` once (interactive), or use [Workers onboarding](https://dash.cloudflare.com/?to=/:account/workers-and-pages) in the dashboard |
| SSL handshake failure / health check after deploy | New subdomain DNS/SSL propagating — wait 2–3 min, then `./demo.sh smoke` or `./demo.sh deploy` (deploy retries health for ~3 min automatically) |
| `wrangler login` expired | `npx wrangler logout && ./demo.sh setup` |
| `403 Forbidden` / `malformed response` on deploy | Often corporate proxy blocking `api.cloudflare.com`. Try personal hotspot, or use **API token** in `.env` (see `.env.example`) then `npx wrangler logout` and `./demo.sh deploy` |
| Collection 404 | Update environment from `./demo.sh urls` |
| Service missing in discovery | Refresh Service discovery; confirm workspace link |
| Test tab empty | Run QA collection first; confirm Test env linkage |
