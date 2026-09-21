# SystemService

A list of all methods in the `SystemService` service. Click on the method name to view detailed information about that method.

| Methods                       | Description |
| :---------------------------- | :---------- |
| [get_health](#get_health)     |             |
| [get_open_api](#get_open_api) |             |

## get_health

- HTTP Method: `GET`
- Endpoint: `/health`

**Return Type**

`HealthResponse`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk

sdk = OrdersSdk(
    timeout=10
)

result = sdk.system.get_health()

print(result)
```

## get_open_api

- HTTP Method: `GET`
- Endpoint: `/openapi.json`

**Return Type**

`OpenApiDocument`

**Example Usage Code Snippet**

```python
from orders_sdk import OrdersSdk

sdk = OrdersSdk(
    timeout=10
)

result = sdk.system.get_open_api()

print(result)
```
