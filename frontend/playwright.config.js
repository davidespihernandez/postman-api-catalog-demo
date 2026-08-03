import { defineConfig, devices } from "@playwright/test";

// Playwright auto-starts the Vite dev server, so `postman app test` (which runs
// `npx playwright test`) is fully self-contained.
export default defineConfig({
  testDir: "./ui-tests",
  timeout: 30_000,
  expect: { timeout: 10_000 },
  fullyParallel: false,
  reporter: [["list"], ["html", { open: "never" }]],
  use: {
    baseURL: "http://localhost:5173",
    trace: "on-first-retry",
  },
  projects: [{ name: "chromium", use: { ...devices["Desktop Chrome"] } }],
  webServer: {
    command: "npm run dev",
    url: "http://localhost:5173",
    reuseExistingServer: !process.env.CI,
    timeout: 60_000,
  },
});
