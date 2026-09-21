# PatchOrderRequest

**Properties**

| Name        | Type                    | Required | Description |
| :---------- | :---------------------- | :------- | :---------- |
| customer_id | str                     | ❌       |             |
| total       | float                   | ❌       |             |
| status      | PatchOrderRequestStatus | ❌       |             |
| currency    | str                     | ❌       |             |

# PatchOrderRequestStatus

**Properties**

| Name       | Type | Required | Description  |
| :--------- | :--- | :------- | :----------- |
| PENDING    | str  | ✅       | "pending"    |
| PROCESSING | str  | ✅       | "processing" |
| SHIPPED    | str  | ✅       | "shipped"    |
| CANCELLED  | str  | ✅       | "cancelled"  |
