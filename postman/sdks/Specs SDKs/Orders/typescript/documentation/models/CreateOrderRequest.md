# CreateOrderRequest

**Properties**

| Name       | Type                     | Required | Description                |
| :--------- | :----------------------- | :------- | :------------------------- |
| customerId | string                   | ✅       | Customer placing the order |
| total      | number                   | ✅       |                            |
| status     | CreateOrderRequestStatus | ❌       | Initial order status       |
| currency   | string                   | ❌       | ISO 4217 currency code     |

# CreateOrderRequestStatus

Initial order status

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| PENDING    | string | ✅       | "pending"    |
| PROCESSING | string | ✅       | "processing" |
| SHIPPED    | string | ✅       | "shipped"    |
| CANCELLED  | string | ✅       | "cancelled"  |
