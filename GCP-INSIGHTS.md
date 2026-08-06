# Self-hosted test API on GCP + Postman Insights (for the API Catalog)

A self-hosted twin of the Cloudflare demo API, on a free GCP VM, where the **Postman Insights
agent** can run — so the **Insights-powered parts of the API Catalog** light up (Runtime Health,
observed endpoints, error rates). The agent can't run on serverless Cloudflare Workers, so it
needs a real VM to attach to.

> Runs **alongside** Cloudflare, not instead of it. You switch backends with Postman environments.
> This is a full replication runbook — any Postman SE can reproduce it from scratch.

## Why GCP (and the cost model)

GCP's **Always Free** tier includes one **`e2-micro`** VM — a real Linux box with root + a public
IP, and e2-micro capacity is reliably available (unlike Oracle's free ARM).

**Cost:** the VM + disk are free, but GCP charges **~$0.005/hr (~$3.60/mo) for the external IPv4
while the VM is running**. A **stopped** VM is **~$0** (IP released, disk within Always Free). So
run it only when needed:

```bash
./control.sh gcp start    # before a demo / data-refresh
./control.sh gcp stop     # after — back to ~$0
```

Insights keeps the **last 7 days** of data, so a couple of days of traffic seeds the catalog for a
week — you often don't even need the VM on *during* a demo.

## Free-tier limits (stay inside these = no surprise charges)

| Resource | Free limit |
|----------|-----------|
| VM | 1× `e2-micro`, only in `us-west1` / `us-central1` / `us-east1` |
| Disk | 30 GB standard persistent disk total |
| Egress | 1 GB/month N. America → most regions (light Insights traffic stays well under) |

A billing account (credit card) is required even for Always Free (verification only).

---

## Part 1 — Project + billing
1. Sign in to the [Google Cloud Console](https://console.cloud.google.com) (a personal Google
   account avoids Workspace admin restrictions).
2. Create a **project** (e.g. `postman-insights`).
3. **Billing → link a card** if you haven't (required for Always Free; no charge within limits).

## Part 2 — Create the VM
Console → **Compute Engine → VM instances → Create instance** (enable the API on first use):
- **Name:** `postman-insights-demo`
- **Region:** `us-central1` / `us-west1` / `us-east1` (must be one of these); any zone. *If a zone
  is "resource exhausted", just pick another zone in the same region.*
- **Machine:** series **E2** → **`e2-micro`**
- **Boot disk:** Ubuntu **22.04 LTS**, **Standard** persistent disk, **30 GB**
- **Firewall:** tick **Allow HTTP traffic** and **Allow HTTPS traffic** ← the whole firewall setup
  (GCP Ubuntu has no host-iptables gotcha; the VPC rule is all you need)
- **Create**, then note the **External IP**.

> Prefer the CLI? `gcloud compute instances create` with `--machine-type=e2-micro
> --image-family=ubuntu-2204-lts --image-project=ubuntu-os-cloud --boot-disk-size=30GB
> --boot-disk-type=pd-standard --tags=http-server,https-server`, plus firewall rules for tcp:80,443.

## Part 3 — SSH in
Browser **SSH** button, or `gcloud compute ssh postman-insights-demo --zone <zone>`, or add your
own key and `ssh -i <key> <user>@<external-ip>`.

## Part 4 — DuckDNS hostname (free, stable across restarts)
The ephemeral IP changes each start/stop, so use a DuckDNS name that the VM re-points on boot:
1. [duckdns.org](https://www.duckdns.org) → sign in → create a subdomain (e.g. `myinsights` →
   `myinsights.duckdns.org`).
2. Copy your **token** (top of the page).

## Part 5 — Grab your Insights IDs (from the catalog)
In **API Catalog → your service → Scorecards**, the panel that shows the agent command gives both
IDs. If it's not visible, the `--system-env` is the **System Environments** entry matching the
scorecard's environment selector (top-right, e.g. **Production**).
- `INSIGHTS_WORKSPACE_ID` = `--workspace-id`
- `INSIGHTS_SYSTEM_ENV`  = `--system-env` (the System Environment's ID)

You'll also need a **Postman API key** (`PMAK-…`) from **Settings → API keys** (the agent
authenticates with it).

## Part 6 — Deploy the stack
The stack in [`runtime-vm/`](runtime-vm/) is cloud-agnostic and auto-adds swap on the 1 GB box.
Full detail in [`runtime-vm/README.md`](runtime-vm/README.md):

```bash
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/davidespihernandez/postman-api-catalog-demo.git
cd postman-api-catalog-demo/runtime-vm
cp .env.example .env && nano .env    # HOSTNAME, DUCKDNS_*, POSTMAN_API_KEY, INSIGHTS_*
./deploy-runtime.sh
```

Then generate traffic (`./synthetic-traffic.sh`, optionally via cron) and watch Runtime Health
populate within ~5–8 minutes.

## Part 7 — Manage it from your laptop
Copy `runtime-vm/gcp/gcp.env.example` → `gcp.env`, fill in your project/zone/instance/host, then:

```bash
./control.sh gcp start | stop | reset | status | urls
./control.sh cf   status | urls          # the Cloudflare side (wraps demo.sh)
./control.sh all  status                 # both backends at a glance
```

---

## Gotchas (already handled in the scripts)
- **Wrangler needs Node ≥ 22** — `deploy-runtime.sh` installs it (Node 20 crash-loops).
- **`apis/*/openapi.json` must be in the repo** — they're committed now (a fresh clone needs them
  to build; they were previously git-ignored).
- **Agent loopback capture** works out of the box (`apidump` listens on `lo`).
- **Ephemeral IP** changing on restart is handled by the DuckDNS updater.

## Security
- Secrets live only in **`runtime-vm/.env`** (on the VM) and **`runtime-vm/gcp/gcp.env`** (your
  laptop) — both git-ignored. Nothing secret is committed.
- On the VM the systemd units embed the API key / DuckDNS token; they're root-readable only.
- If a key was ever shared in plaintext (chat, screen-share), rotate it in Postman afterwards.
