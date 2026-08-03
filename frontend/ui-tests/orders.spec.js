import { test as baseTest, expect } from "@playwright/test";
import { attachNetworkCapture } from "postman-playwright";

// Wrap the base fixture so Postman captures every browser -> API call made
// during the test and matches it against the linked "Orders - QA" collection.
const test = attachNetworkCapture(baseTest);

test("create, advance status, and delete an order via the UI", async ({ page }) => {
  await page.goto("/");
  await expect(page.getByRole("heading", { name: "Orders", exact: true })).toBeVisible();

  // Initial list load -> GET /orders
  await expect(page.getByTestId("orders-table")).toBeVisible();

  const customerId = "usr-playwright";
  await page.getByTestId("input-customerId").fill(customerId);
  await page.getByTestId("input-total").fill("249.99");

  // Create -> POST /orders. Grab the generated id from the response.
  const [createRes] = await Promise.all([
    page.waitForResponse(
      (r) => r.url().endsWith("/orders") && r.request().method() === "POST",
    ),
    page.getByTestId("submit-order").click(),
  ]);
  expect(createRes.status()).toBe(201);
  const { id: orderId } = await createRes.json();

  const row = page.getByTestId(`order-row-${orderId}`);
  await expect(row).toBeVisible();
  await expect(row).toContainText(customerId);

  // Advance status -> PATCH /orders/{id}
  await Promise.all([
    page.waitForResponse(
      (r) => r.url().includes(`/orders/${orderId}`) && r.request().method() === "PATCH",
    ),
    page.getByTestId(`advance-${orderId}`).click(),
  ]);
  await expect(row).not.toContainText("pending");

  // Delete -> DELETE /orders/{id}. The API replies 204 (no body); Chromium
  // reports empty 204s as ERR_ABORTED, which makes waitForResponse flaky, so we
  // assert on the UI outcome (row removed after the delete + refresh) instead.
  await page.getByTestId(`delete-${orderId}`).click();
  await expect(page.getByTestId(`order-row-${orderId}`)).toHaveCount(0);
});
