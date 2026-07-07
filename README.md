# Postman API Catalog Demo

Real HTTPS APIs on **Cloudflare Workers** (free tier) for repeatable **API Catalog** demos.

```
Setup (terminal once)  →  Cloudflare Workers
Customer demo          →  Postman app only (catalog + collections)
```

Repository: [github.com/davidespihernandez/postman-api-catalog-demo](https://github.com/davidespihernandez/postman-api-catalog-demo)

| Doc | Audience |
|-----|----------|
| [SE-INSTALL.md](SE-INSTALL.md) | SE one-time setup (includes deploy) |
| [DEMO-STEPS.md](DEMO-STEPS.md) | Customer demo (Postman app only) |

## Setup (before the demo)

```bash
./demo.sh setup
./demo.sh setup-subdomain   # one-time: pick your *.workers.dev name
./demo.sh deploy
./demo.sh urls
```

`setup-subdomain` must run in an **interactive terminal** (wrangler prompts for your subdomain). After that, `deploy` is non-interactive.

Sync the `postman/` folder into your workspace (collections, environments, globals). See [SE-INSTALL.md](SE-INSTALL.md).

## Postman assets

```
postman/collections/     QA + Doc collections (native Postman format)
postman/environments/    Production / Mock per API (`baseUrl`)
postman/globals/
```

| Collection | Purpose |
|------------|---------|
| `Orders - QA`, `Payments - QA`, `Users - QA` | CRUD validation |
| `Payments - Doc` → Refund a payment | Refund + worker webhook demo |

Set `REFUND_WEBHOOK_URL` in `.env` before deploy (see SE-INSTALL).

## Requirements

Node.js 18+, curl, Cloudflare (free), Postman Enterprise with API Catalog.
