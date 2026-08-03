import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev server runs on 5173; Playwright's webServer config points here.
export default defineConfig({
  plugins: [react()],
  server: { port: 5173 },
  preview: { port: 4173 },
});
