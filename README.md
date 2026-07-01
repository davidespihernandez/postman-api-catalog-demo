# Postman API Catalog Demo

Real HTTPS APIs on **Cloudflare Workers** (free tier), orchestrated for repeatable **API Catalog** customer demos.

```
Postman (collection / monitor)  →  Cloudflare Workers  →  API Catalog
```

Repository: [github.com/davidespihernandez/postman-api-catalog-demo](https://github.com/davidespihernandez/postman-api-catalog-demo)

## Quick start

```bash
cp .env.example .env
# Add POSTMAN_API_KEY to .env (Enterprise API Catalog)

./demo.sh setup              # once: npm install + wrangler login
./demo.sh baseline           # State 1: deploy Orders
./demo.sh register orders    # push OpenAPI to API Catalog
./demo.sh add payments       # State 2: live-deploy Payments during demo
./demo.sh traffic-demo       # State 3: mixed success/latency/error traffic
```

Full presenter script: [catalog/DEMO-STEPS.md](catalog/DEMO-STEPS.md)

## What you get

| API | Endpoints |
|-----|-----------|
| **Orders** | `GET/POST /orders`, `GET /orders/{id}`, `/health`, `/openapi.json` |
| **Payments** | `GET/POST /payments`, `POST /payments/refund`, `/health`, `/openapi.json` |
| **Users** | `GET /users`, `GET /users/{id}`, `/health`, `/openapi.json` |

In-memory data only — no database. Each deploy resets worker state.

### Demo query parameters

On any endpoint except `/openapi.json`:

| Param | Effect |
|-------|--------|
| `?delay=2000` | Wait 2s before responding (max 30000 ms) |
| `?error=500` | Return HTTP 500 with a simulated error body |

Use these in monitors/collections to populate **Test** tab metrics in the API Catalog.

## Demo commands

| Command | Purpose |
|---------|---------|
| `./demo.sh setup` | One-time Cloudflare login + npm install |
| `./demo.sh reset` | Remove Payments worker; prep for fresh demo |
| `./demo.sh baseline` | Deploy Orders + smoke test |
| `./demo.sh add payments` | Deploy Payments (live demo moment) |
| `./demo.sh register <api>` | Register service in API Catalog via Postman API |
| `./demo.sh register all` | Register every deployed API |
| `./demo.sh traffic-demo` | Send mixed OK/slow/error traffic to workers |
| `./demo.sh smoke` | Health-check deployed APIs |
| `./demo.sh urls` | Show worker URLs from last deploy |

## Postman collection

Import into Postman:

- `postman/demo.collection.json`
- `postman/demo.environment.json`

Update worker URLs (paste from `./demo.sh urls`).

## Manual setup (one time)

1. **Cloudflare account** — free; `wrangler login` during `./demo.sh setup`
2. **Postman Enterprise** with API Catalog
3. **`POSTMAN_API_KEY`** in `.env` for `./demo.sh register`
4. **Integrate services** in API Catalog → Service discovery (UI step after register)
5. Optional: **monitor** + **scorecards** / governance groups for quality demo

## Project layout

```
apis/orders|payments|users/   Cloudflare Workers + OpenAPI
scripts/                      deploy, smoke, register, traffic helpers
postman/                      Collection + environment
catalog/                      Presenter steps
demo.sh                       Main entrypoint
```

## Requirements

- Node.js 18+
- `curl`
- Cloudflare account (free)
- Postman Enterprise team with API Catalog

## Cost

Cloudflare Workers free tier: 100k requests/day — more than enough for demos.
