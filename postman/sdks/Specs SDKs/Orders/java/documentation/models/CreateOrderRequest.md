# CreateOrderRequest

**Properties**

| Name       | Type                     | Required | Description                |
| :--------- | :----------------------- | :------- | :------------------------- |
| customerId | String                   | ✅       | Customer placing the order |
| total      | Double                   | ✅       |                            |
| status     | CreateOrderRequestStatus | ❌       | Initial order status       |
| currency   | String                   | ❌       | ISO 4217 currency code     |
