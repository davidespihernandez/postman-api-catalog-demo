# Self-hosted test API on GCP + Postman Insights (for the API Catalog)

Same goal as [`ORACLE-INSIGHTS.md`](ORACLE-INSIGHTS.md) — a self-hosted twin of the Cloudflare API
where the **Postman Insights agent** can run, so the Insights-powered parts of the API Catalog
(Runtime Health, observed endpoints, error rates) light up. This is the **GCP alternative**, used
because Oracle's free ARM (A1) capacity is unobtainable in some regions.

> Both backends stay live alongside Cloudflare — this is additive, not a migration.

## Why GCP works here

GCP's **Always Free** tier includes **one `e2-micro` VM, free forever** — a real Linux VM with
root and a public IP, so the agent can run (unlike serverless free tiers). No capacity lottery
like Oracle A1. Trade-off: **e2-micro is ~1 GB RAM**, so `deploy-runtime.sh` auto-adds a 2 GB
swapfile. Fine for a demo; the 20-VU perf test may run slower.

## Free-tier limits to respect (stay inside these = no charge)

| Resource | Free-forever limit |
|----------|--------------------|
| VM | 1× `e2-micro`, **only** in `us-west1`, `us-central1`, or `us-east1` |
| Disk | 30 GB standard persistent disk total |
| Egress | 1 GB/month North America → most regions (watch this — see below) |

A billing account (credit card) is required, but Always Free usage doesn't charge as long as you
stay within the limits above. (New accounts also get a separate $300/90-day credit.)

> **Egress note:** the Insights agent sends observed traffic data to Postman's cloud — that's
> egress. Light/synthetic demo traffic stays well under 1 GB/month; just don't blast heavy load
> at it continuously.

---

## Part 1 — Project + billing

1. Sign in to the [Google Cloud Console](https://console.cloud.google.com).
2. Create (or pick) a **project** — e.g. `postman-insights`.
3. Set up a **billing account** if you don't have one (Billing → link a card). Required even for
   Always Free.

## Part 2 — Create the VM

1. ☰ menu → **Compute Engine → VM instances**. First time: click **Enable** (Compute Engine API).
2. **Create instance**:
   - **Name:** `postman-insights-demo`
   - **Region:** `us-central1` (or `us-west1` / `us-east1` — **must** be one of these for free
     tier). **Zone:** any, e.g. `us-central1-a`.
   - **Machine configuration:** series **E2** → **`e2-micro` (2 vCPU, 1 GB)** — the free-tier
     eligible shape.
   - **Boot disk** → **Change**: OS **Ubuntu**, version **Ubuntu 22.04 LTS**, boot disk type
     **Standard persistent disk**, size **30 GB** (stay within the free 30 GB).
   - **Firewall:** tick **Allow HTTP traffic** and **Allow HTTPS traffic** (this creates the
     firewall rules for ports 80/443 automatically).
3. **Create.** When it's running, copy the **External IP** from the instances list.

> Unlike Oracle, GCP's Ubuntu images have **no restrictive host iptables** — opening 80/443 in the
> VPC firewall (the two checkboxes above) is all you need. One firewall, not two.

## Part 3 — SSH in

Easiest is the browser: click **SSH** next to the instance in the console. Or from your laptop
with the gcloud CLI:

```bash
gcloud compute ssh postman-insights-demo --zone us-central1-a
```

(If you didn't tick the firewall boxes at creation: **VPC network → Firewall → Create firewall
rule**, allow `tcp:80,443` from `0.0.0.0/0`, target all instances or the instance's network tag.)

## Part 4 — Point a hostname at the VM (for TLS)

Caddy needs a hostname to fetch a Let's Encrypt cert. Same as Oracle:

- **nip.io (zero signup):** for external IP `34.71.5.9`, use `34-71-5-9.nip.io`. Each API is then
  `orders.34-71-5-9.nip.io`, etc.
- **DuckDNS:** create a subdomain pointed at the external IP for a stable name.

## Part 5 — Deploy the stack (Part 2, shared with Oracle)

Identical to the Oracle path — the stack in [`runtime-vm/`](runtime-vm/) is cloud-agnostic and
auto-adds swap on this 1 GB box:

```bash
sudo apt-get update && sudo apt-get install -y git
git clone https://github.com/davidespihernandez/postman-api-catalog-demo.git
cd postman-api-catalog-demo/runtime-vm
cp .env.example .env
nano .env          # HOSTNAME (your nip.io/DuckDNS name), POSTMAN_API_KEY, INSIGHTS_WORKSPACE_ID, INSIGHTS_SYSTEM_ENV
./deploy-runtime.sh
```

Then follow **Verify** and **Wire up the Postman environments + CI** in
[`runtime-vm/README.md`](runtime-vm/README.md). Everything after this point is the same as Oracle.

---

## Troubleshooting

| Symptom | Fix |
|---------|-----|
| `e2-micro` not free / charged | Must be in `us-west1`/`us-central1`/`us-east1`, only **one** such VM, standard PD ≤ 30 GB. |
| Caddy TLS cert fails | Hostname must resolve to the External IP (`dig +short orders.$HOSTNAME`); HTTP/HTTPS firewall must be on. |
| Stack sluggish / OOM | It's 1 GB — `deploy-runtime.sh` adds 2 GB swap automatically; confirm with `swapon --show`. For a lighter footprint, run only the Orders API. |
| Approaching 1 GB egress | Reduce synthetic-traffic frequency; it's the agent uploading observed data. |
