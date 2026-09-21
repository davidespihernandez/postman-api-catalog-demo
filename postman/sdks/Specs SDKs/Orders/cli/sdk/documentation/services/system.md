# System

A list of all methods in the `System` service. Click on the method name to view detailed information about that method.

| Methods                   | Description |
| :------------------------ | :---------- |
| [GetHealth](#gethealth)   |             |
| [GetOpenAPI](#getopenapi) |             |

## GetHealth

- HTTP Method: `GET`
- Endpoint: `/health`

**Parameters**

| Name | Type    | Required | Description                 |
| :--- | :------ | :------- | :-------------------------- |
| ctx  | Context | ✅       | Default go language context |

**Return Type**

`HealthResponse`

**Example Usage Code Snippet**

```go
orders system get-health
```

## GetOpenAPI

- HTTP Method: `GET`
- Endpoint: `/openapi.json`

**Parameters**

| Name | Type    | Required | Description                 |
| :--- | :------ | :------- | :-------------------------- |
| ctx  | Context | ✅       | Default go language context |

**Return Type**

`OpenAPIDocument`

**Example Usage Code Snippet**

```go
orders system get-open-api
```
