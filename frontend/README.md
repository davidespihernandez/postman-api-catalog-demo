# Orders UI + Playwright — Postman Browser Testing Demo

A minimal React (Vite) frontend for the Cloudflare **Orders** API, plus a
Playwright test wired into **Postman's browser testing** integration. The point
of the demo: drive the UI in a real browser, and have Postman capture every
browser → API call and match it against your `Orders - QA` collection in the
**Application Inventory** dashboard.

```
frontend/
├── src/               React SPA (list / create / advance-status / delete orders)
├── ui-tests/          Playwright spec, wrapped with attachNetworkCapture
├── playwright.config.js   auto-starts the Vite dev server on :5173
└── postman.config.cjs     links the run to the Orders - QA collection + env
```

## Prerequisites

- Node 18+
- The Cloudflare Orders worker deployed (default:
  `https://postman-api-catalog-demo-orders.davidespi.workers.dev`)
- Postman CLI + a Postman account with **Application Inventory / browser testing**
  enabled

## Setup

```bash
cd frontend
npm install
npx playwright install chromium
cp .env.example .env      # optional — override VITE_ORDERS_API_URL
```

## Run the UI locally

```bash
npm run dev               # http://localhost:5173
```

## Run the Playwright test (no Postman)

```bash
npx playwright test       # boots the dev server automatically
```

## Run WITH Postman capture (the demo)

```bash
npm install -g postman-cli
postman login                       # or set POSTMAN_API_KEY
postman app init                    # confirm collection = "Orders - QA",
                                    #   environment = "Production - Orders",
                                    #   test command  = "npx playwright test"
CI=true postman app test            # runs Playwright, captures traffic
```

Then open **Application Inventory** in Postman to show the captured Orders
requests matched against the collection (matched vs. not-matched = live proof
the UI exercises the documented API contract).

> `postman.config.cjs` is pre-filled — edit the collection/environment names to
> match your workspace exactly if `postman app init` doesn't overwrite them.

## Deploy the UI to Cloudflare Pages (optional)

```bash
npm run deploy            # builds and runs `wrangler pages deploy dist`
```

## Notes

- The Orders API stores data **in memory**, so it resets to two seed rows on
  every worker cold start / redeploy. The test creates its own order, so it is
  self-contained.
- `DELETE` returns `204`; Chromium reports empty 204s as `ERR_ABORTED`, so the
  test asserts on the UI (row removed) rather than `waitForResponse`.
