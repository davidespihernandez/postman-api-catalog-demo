# Agent context: postman-api-catalog-demo

Use this file when continuing work in a new chat or workspace. Chat history may not carry over between Cursor windows.

GitHub: https://github.com/davidespihernandez/postman-api-catalog-demo

## Purpose

Build and maintain a **realistic, repeatable customer demo** of the **Postman API Catalog**. The demo uses **real HTTPS APIs** deployed on **free cloud infrastructure** (Cloudflare Workers), not mocks or localhost.

Primary users: Postman Solutions Engineers presenting service discovery, catalog integration, spec quality, and test metrics to customers.

## What we want to achieve

The audience should see a production-like story:

1. Real APIs deployed in the cloud (Cloudflare Workers).
2. Services **registered and integrated** in the API Catalog (OpenAPI from `/openapi.json`).
3. A **new API deployed live** during the presentation (Payments).
4. The new API **appearing in API Catalog** after `./demo.sh register payments` + UI integrate.
5. **Quality and metrics** via scorecards, spec governance, and collection/monitor runs (Test tab).

### Design philosophy

- **Do not create cloud accounts during the presentation.** Pre-create Cloudflare + Postman connections once; redeploy worker versions during demos.
- **Zero budget.** Cloudflare Workers free tier only; no database.
- **Easy to reset and repeat.** One or two commands between customer meetings.
- **Reliable over flashy.** Scripts should fail loudly with clear errors, not half-succeed silently.

## Architecture

```
Postman (collection / monitor / curl)
        ↓
Cloudflare Workers           ← Orders, Payments, Users (in-memory, no DB)
        ↓
API Catalog                  ← discovery, spec quality, test metrics, scorecards
```

Each worker exposes:

- `/health` — smoke tests and presenter checks
- `/openapi.json` — OpenAPI 3.0 spec (server URL set dynamically from request origin)
- 2–3 realistic REST endpoints with seeded in-memory data
- Optional `?delay=` and `?error=` query params for simulated latency/errors (catalog Test tab demos)

### Demo APIs

| API | Worker name | Key endpoints |
|-----|-------------|---------------|
| Orders | `postman-api-catalog-demo-orders` | `GET/POST /orders`, `GET /orders/{id}` |
| Payments | `postman-api-catalog-demo-payments` | `GET/POST /payments`, `POST /payments/refund` |
| Users | `postman-api-catalog-demo-users` | `GET /users`, `GET /users/{id}` |

**Baseline state (State 1):** only Orders is deployed + registered.  
**Live demo moment (State 2):** deploy Payments with `./demo.sh add payments` + register.  
**Quality (State 3):** `./demo.sh traffic-demo` + Postman collection/monitor → catalog Test tab + scorecards.

**Note:** Production runtime metrics (p95, error rate on the Production tab) require the Postman Insights Agent on Kubernetes. This demo focuses on **spec quality** + **Test** tab metrics without K8s.

## Repository layout

- **`demo.sh`** — Main entrypoint (`setup`, `reset`, `baseline`, `add`, `register`, `traffic-demo`, `smoke`, `urls`).
- **`apis/orders/`**, **`apis/payments/`**, **`apis/users/`** — Each contains `src/worker.mjs`, `openapi.json`, `wrangler.toml`.
- **`apis/shared/http.mjs`** — JSON responses, CORS, OpenAPI helper, demo query-param simulation.
- **`scripts/`** — `lib.sh`, `deploy-api.sh`, `undeploy-api.sh`, `smoke-test.sh`, `register-api.mjs`, `traffic-demo.sh`.
- **`postman/`** — `demo.collection.json`, `demo.environment.json` for import into Postman.
- **`catalog/DEMO-STEPS.md`** — Human-facing presenter script.
- **`README.md`** — Human-facing quick start.
- **`.env.example`** — Committed config template. **`.env`** and **`.demo-state.env`** are gitignored.

## Runtime and dependencies

- **Node.js 18+** (global `fetch` for `register-api.mjs`).
- **ESM** (`"type": "module"`); workers and scripts use `.mjs`.
- **`wrangler`** (devDependency) — deploy/delete Cloudflare Workers.
- **`curl`** — smoke tests and `traffic-demo`.
- **Bash** — `demo.sh` and shell scripts (macOS-compatible; `sed -i ''` used intentionally).

```bash
./demo.sh setup
./demo.sh reset && ./demo.sh baseline && ./demo.sh register orders
./demo.sh add payments && ./demo.sh register payments
./demo.sh traffic-demo
```

## Configuration

| Variable | Where | Purpose |
|----------|-------|---------|
| `POSTMAN_API_KEY` | `.env` | Required for `./demo.sh register <api>` (API Catalog, Enterprise) |
| `DEMO_ENVIRONMENT` | `.env` | Label for catalog registration (default: `demo`) |
| `ORDERS_API_URL`, `PAYMENTS_API_URL`, `USERS_API_URL` | `.demo-state.env` | Written by `deploy-api.sh` after `wrangler deploy` |

**Never commit `.env`, `.demo-state.env`, or Cloudflare credentials.**

## Implemented commands (`demo.sh`)

| Command | Behavior |
|---------|----------|
| `setup` | `npm install` + `wrangler login` |
| `reset` | Undeploy Payments worker |
| `baseline` | Deploy Orders + smoke test (State 1) |
| `add <api>` | Deploy `payments` or `users` + smoke test (State 2) |
| `register <api\|all>` | Fetch `/openapi.json`, POST to API Catalog discovery-services |
| `traffic-demo` | Mixed OK/slow/error curls against deployed workers |
| `smoke [api\|all]` | Health-check deployed workers |
| `urls` | Print `.demo-state.env` worker URLs |

## What is automated vs manual

### Automated (scripts)

- Cloudflare Worker deploy/undeploy
- Health smoke tests
- Worker URL capture into `.demo-state.env`
- API Catalog registration via `POST /api-catalog/discovery-services`
- Simulated traffic for metrics demos (`traffic-demo`)

### Manual (presenter / one-time setup)

- Cloudflare account creation and `wrangler login`
- **API Catalog** Service discovery → integrate registered services (Enterprise)
- Postman collection environment variable updates after first deploy
- Optional: monitors, scorecards, governance groups
- Clearing stale Payments entries from catalog between demos

## Postman integration

- **Collection:** `postman/demo.collection.json` — smoke tests + catalog metrics demo folder.
- **Environment:** `postman/demo.environment.json` — worker URL variables.
- **API Catalog API:** `scripts/register-api.mjs` uses `X-Api-Key` against `https://api.getpostman.com`.

## Engineering preferences

- **Minimize scope.** Demo kit, not a production platform.
- **Keep workers self-contained.** Small duplication across workers is acceptable.
- **OpenAPI is source of truth.** Keep `openapi.json` in sync with handler routes.
- **Match existing patterns** in `apis/shared/` and `scripts/lib.sh`.
- **Shell scripts must be idempotent** where possible.
- **Fail with actionable messages.**

## Adding a new demo API

1. Copy `apis/orders/` structure to `apis/<name>/`.
2. Update `openapi.json`, `worker.mjs`, `wrangler.toml` (unique worker `name`).
3. Add cases to `scripts/lib.sh` (`api_name`, `worker_name`).
4. Extend `demo.sh add` validation and Postman collection/environment.
5. Document in `catalog/DEMO-STEPS.md` if it changes the demo narrative.

## Common agent tasks

| User asks for… | Start here |
|----------------|------------|
| Fix deploy or wrangler errors | `scripts/deploy-api.sh`, `apis/*/wrangler.toml` |
| Add endpoint to an API | `apis/<api>/src/worker.mjs` + `openapi.json` |
| Improve demo repeatability | `demo.sh`, `catalog/DEMO-STEPS.md` |
| Postman collection updates | `postman/demo.collection.json`, environment |
| Catalog registration issues | `scripts/register-api.mjs`, `POSTMAN_API_KEY`, Enterprise access |
