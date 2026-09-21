# UpdateOrderRequest

**Properties**

| Name       | Type                     | Required | Description |
| :--------- | :----------------------- | :------- | :---------- |
| customerId | string                   | ✅       |             |
| total      | number                   | ✅       |             |
| status     | UpdateOrderRequestStatus | ✅       |             |
| currency   | string                   | ❌       |             |

# UpdateOrderRequestStatus

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| PENDING    | string | ✅       | "pending"    |
| PROCESSING | string | ✅       | "processing" |
| SHIPPED    | string | ✅       | "shipped"    |
| CANCELLED  | string | ✅       | "cancelled"  |
