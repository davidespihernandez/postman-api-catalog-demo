import { rm } from "node:fs/promises";
import path from "node:path";

// Clear stale network captures before each run. postman-playwright writes
// network-capture-*.ndjson into <cwd>/pm-results, and `postman app test` scans
// ALL of them — so without this, repeated runs accumulate old captures and
// inflate the matched/not-matched counts.
export default async function globalSetup() {
  await rm(path.join(process.cwd(), "pm-results"), { recursive: true, force: true });
}
