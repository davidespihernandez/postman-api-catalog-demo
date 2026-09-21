# SystemService

A list of all methods in the `SystemService` service. Click on the method name to view detailed information about that method.

| Methods                   | Description |
| :------------------------ | :---------- |
| [getHealth](#gethealth)   |             |
| [getOpenApi](#getopenapi) |             |

## getHealth

- HTTP Method: `GET`
- Endpoint: `/health`

**Return Type**

`HealthResponse`

**Example Usage Code Snippet**

```typescript
import { OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const data = await ordersSdk.system.getHealth();

  console.log(data);
})();
```

## getOpenApi

- HTTP Method: `GET`
- Endpoint: `/openapi.json`

**Return Type**

`OpenApiDocument`

**Example Usage Code Snippet**

```typescript
import { OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const data = await ordersSdk.system.getOpenApi();

  console.log(data);
})();
```
