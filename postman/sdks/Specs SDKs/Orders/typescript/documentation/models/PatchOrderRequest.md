# PatchOrderRequest

**Properties**

| Name       | Type                    | Required | Description |
| :--------- | :---------------------- | :------- | :---------- |
| customerId | string                  | ❌       |             |
| total      | number                  | ❌       |             |
| status     | PatchOrderRequestStatus | ❌       |             |
| currency   | string                  | ❌       |             |

# PatchOrderRequestStatus

**Properties**

| Name       | Type   | Required | Description  |
| :--------- | :----- | :------- | :----------- |
| PENDING    | string | ✅       | "pending"    |
| PROCESSING | string | ✅       | "processing" |
| SHIPPED    | string | ✅       | "shipped"    |
| CANCELLED  | string | ✅       | "cancelled"  |
