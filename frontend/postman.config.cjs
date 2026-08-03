// Links this app's Playwright run to Postman. `postman app init` normally
// generates this file interactively; the values below are pre-filled for the
// Orders demo. Adjust collection / environment names to match your workspace
// exactly (run `postman app init` to pick them from a list if unsure).
module.exports = {
  command: "npx playwright test",
  targets: {
    default: {
      environment: "Production - Orders",
      collections: ["Orders - QA"],
    },
  },
  filters: {
    // Ignore non-API noise so Application Inventory only shows Orders traffic.
    urlPatterns: ["localhost:5173", "fonts.googleapis.com", "fonts.gstatic.com"],
    methods: [],
    headers: {},
  },
};
