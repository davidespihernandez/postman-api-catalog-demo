# CreateOrderRequest

**Properties**

| Name        | Type                     | Required | Description                |
| :---------- | :----------------------- | :------- | :------------------------- |
| customer_id | str                      | ✅       | Customer placing the order |
| total       | float                    | ✅       |                            |
| status      | CreateOrderRequestStatus | ❌       | Initial order status       |
| currency    | str                      | ❌       | ISO 4217 currency code     |

# CreateOrderRequestStatus

Initial order status

**Properties**

| Name       | Type | Required | Description  |
| :--------- | :--- | :------- | :----------- |
| PENDING    | str  | ✅       | "pending"    |
| PROCESSING | str  | ✅       | "processing" |
| SHIPPED    | str  | ✅       | "shipped"    |
| CANCELLED  | str  | ✅       | "cancelled"  |
