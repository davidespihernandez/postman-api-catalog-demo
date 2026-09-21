# OrdersService

A list of all methods in the `OrdersService` service. Click on the method name to view detailed information about that method.

| Methods                       | Description |
| :---------------------------- | :---------- |
| [list_orders](#list_orders)   |             |
| [create_order](#create_order) |             |
| [get_order](#get_order)       |             |
| [update_order](#update_order) |             |
| [patch_order](#patch_order)   |             |
| [delete_order](#delete_order) |             |

## list_orders

- HTTP Method: `GET`
- Endpoint: `/orders`

**Return Type**

`OrderListResponse`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk

sdk = OrdersSdk(
    timeout=10
)

result = sdk.orders.list_orders()

print(result)
```

## create_order

- HTTP Method: `POST`
- Endpoint: `/orders`

**Parameters**

| Name         | Type                                                  | Required | Description       |
| :----------- | :---------------------------------------------------- | :------- | :---------------- |
| request_body | [CreateOrderRequest](../models/CreateOrderRequest.md) | ✅       | The request body. |

**Return Type**

`Order`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk
from orders_sdk.models import CreateOrderRequest

sdk = OrdersSdk(
    timeout=10
)

request_body = CreateOrderRequest(
    customer_id="usr-002",
    total=99.5,
    status="pending",
    currency="USD"
)

result = sdk.orders.create_order(request_body=request_body)

print(result)
```

## get_order

- HTTP Method: `GET`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type | Required | Description      |
| :--- | :--- | :------- | :--------------- |
| id\_ | str  | ✅       | Order identifier |

**Return Type**

`Order`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk

sdk = OrdersSdk(
    timeout=10
)

result = sdk.orders.get_order(id_="ord-001")

print(result)
```

## update_order

- HTTP Method: `PUT`
- Endpoint: `/orders/{id}`

**Parameters**

| Name         | Type                                                  | Required | Description       |
| :----------- | :---------------------------------------------------- | :------- | :---------------- |
| request_body | [UpdateOrderRequest](../models/UpdateOrderRequest.md) | ✅       | The request body. |
| id\_         | str                                                   | ✅       | Order identifier  |

**Return Type**

`Order`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk
from orders_sdk.models import UpdateOrderRequest

sdk = OrdersSdk(
    timeout=10
)

request_body = UpdateOrderRequest(
    customer_id="usr-003",
    total=149.99,
    status="pending",
    currency="EUR"
)

result = sdk.orders.update_order(
    request_body=request_body,
    id_="ord-001"
)

print(result)
```

## patch_order

- HTTP Method: `PATCH`
- Endpoint: `/orders/{id}`

**Parameters**

| Name         | Type                                                | Required | Description       |
| :----------- | :-------------------------------------------------- | :------- | :---------------- |
| request_body | [PatchOrderRequest](../models/PatchOrderRequest.md) | ✅       | The request body. |
| id\_         | str                                                 | ✅       | Order identifier  |

**Return Type**

`Order`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk
from orders_sdk.models import PatchOrderRequest

sdk = OrdersSdk(
    timeout=10
)

request_body = PatchOrderRequest(
    customer_id="usr-003",
    total=149.99,
    status="pending",
    currency="EUR"
)

result = sdk.orders.patch_order(
    request_body=request_body,
    id_="ord-001"
)

print(result)
```

## delete_order

- HTTP Method: `DELETE`
- Endpoint: `/orders/{id}`

**Parameters**

| Name | Type | Required | Description      |
| :--- | :--- | :------- | :--------------- |
| id\_ | str  | ✅       | Order identifier |

**Return Type**

`ErrorResponse`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk

sdk = OrdersSdk(
    timeout=10
)

result = sdk.orders.delete_order(id_="ord-001")

print(result)
```
