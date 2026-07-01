# API Catalog demo steps (one-time + per-demo)

Repository: [github.com/davidespihernandez/postman-api-catalog-demo](https://github.com/davidespihernandez/postman-api-catalog-demo)

## One-time setup (before first customer demo)

### 1. Cloudflare

```bash
cp .env.example .env
./demo.sh setup
```

Installs dependencies and runs `wrangler login` (browser opens once).

### 2. Postman API Catalog

Requirements:

- Postman **Enterprise** plan with API Catalog access
- `POSTMAN_API_KEY` in `.env` (team key with API Catalog permissions)

One-time in Postman:

1. Import `postman/demo.collection.json` and `postman/demo.environment.json`
2. After first deploy, paste worker URLs from `./demo.sh urls` into the environment
3. Optional: create a **Monitor** from the collection and link it to your catalog **Test** system environment
4. Optional: configure **governance groups** and **scorecards** for OpenAPI spec quality

Docs: [Connect API Catalog to your services](https://learning.postman.com/docs/api-catalog/connect/overview)

### 3. Baseline deploy + register Orders

```bash
./demo.sh baseline
./demo.sh register orders
```

In Postman: **API Catalog → Service discovery** → integrate **Orders API** into the catalog.

---

## Demo states

| State | What's live | Commands |
|-------|-------------|----------|
| **1 — Baseline** | Orders API only | `./demo.sh reset && ./demo.sh baseline && ./demo.sh register orders` |
| **2 — New API** | + Payments API | `./demo.sh add payments && ./demo.sh register payments` |
| **3 — Quality & metrics** | Monitors / collection runs | `./demo.sh traffic-demo` + run collection in Postman |

---

## Live demo script (~15 min)

### Opening (State 1)

```bash
./demo.sh reset
./demo.sh baseline
./demo.sh urls
./demo.sh register orders
```

**Show:**

- API Catalog with **Orders API** integrated
- `GET {{ordersApiUrl}}/orders` in Postman
- OpenAPI at `{{ordersApiUrl}}/openapi.json`
- Spec quality / scorecard on the service (if configured)

### Deploy new API (State 2)

```bash
./demo.sh add payments
./demo.sh register payments
```

**Show:**

1. Terminal deploy (`wrangler deploy` — real cloud deploy)
2. `GET {{paymentsApiUrl}}/health`
3. **Service discovery** — **Payments API** appears; integrate in the UI
4. Catalog list now shows Orders + Payments

### Quality & metrics (State 3)

```bash
./demo.sh traffic-demo
```

Then in Postman, run the **Catalog metrics demo** folder (or your monitor).

**Show:**

- Collection tests with `?delay=` and `?error=` query params
- API Catalog **Test** tab: pass rate, latency, assertions (after monitor/collection runs)
- **Development** tab: spec linting / governance violations (if rules configured)
- Scorecard combining spec + test quality

**Note:** Production runtime metrics (p95, error rate on the **Production** tab) require the [Postman Insights Agent](https://learning.postman.com/docs/api-catalog/connect/insights) on Kubernetes. This demo uses Cloudflare Workers and focuses on **spec quality** + **Test** tab metrics via monitors/collections.

---

## Reset between demos

```bash
./demo.sh reset
./demo.sh baseline
./demo.sh register orders
```

Remove stale **Payments** entries from Service discovery in the catalog UI if needed.

---

## Troubleshooting

| Issue | Fix |
|-------|-----|
| `wrangler login` expired | `./demo.sh setup` |
| Worker URL not detected | Set `ORDERS_API_URL` etc. in `.demo-state.env` |
| `register` fails (403/404) | Enterprise API Catalog + valid `POSTMAN_API_KEY`; or integrate manually in Service discovery UI |
| Test tab empty | Run collection/monitor against worker URLs; link environment to catalog Test system environment |
| Slow/error simulation ignored | Use `?delay=2000` or `?error=500` on worker URLs (not `/openapi.json`) |
