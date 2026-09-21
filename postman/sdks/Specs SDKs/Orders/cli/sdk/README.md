# OrdersSDK Go SDK 1.0.0

Welcome to the OrdersSDK SDK documentation. This guide will help you get started with integrating and using the OrdersSDK SDK in your project.

## Versions

- SDK version: `1.0.0`

## About the API

Demo orders service for the Postman API Catalog walkthrough.

Optional query parameters on all endpoints except /openapi.json:

- delay or wait: milliseconds to wait before responding (max 30000)
- status or error: HTTP status code to return (e.g. 500); error=true also returns 500

## Table of Contents

- [Setup & Configuration](#setup--configuration)
  - [Supported Language Versions](#supported-language-versions)
- [Setting a Custom Timeout](#setting-a-custom-timeout)
- [Sample Usage](#sample-usage)
- [Services](#services)
  - [Response Wrappers](#response-wrappers)
- [Models](#models)

# Setup & Configuration

## Supported Language Versions

This SDK is compatible with the following versions: `Go >= 1.19.0`

## Setting a Custom Timeout

You can set a custom timeout for the SDK's HTTP requests as follows:

```go

```

# Sample Usage

Below is a comprehensive example demonstrating how to authenticate and call a simple endpoint:

```go
orders system get-health

```

## Services

The SDK provides various services to interact with the API.

<details>
<summary>Below is a list of all available services with links to their detailed documentation:</summary>

| Name                                       |
| :----------------------------------------- |
| [System](documentation/services/system.md) |
| [Orders](documentation/services/orders.md) |

</details>

### Response Wrappers

All services use response wrappers to provide a consistent interface to return the responses from the API.

The response wrapper itself is a generic struct that contains the response data and metadata.

<details>
<summary>Below are the response wrappers used in the SDK:</summary>

#### `OrdersSDKResponse[T]`

This response wrapper is used to return the response data from the API. It contains the following fields:

| Name     | Type                        | Description                                 |
| :------- | :-------------------------- | :------------------------------------------ |
| Data     | `T`                         | The body of the API response                |
| Metadata | `OrdersSDKResponseMetadata` | Status code and headers returned by the API |

#### `OrdersSDKError[T]`

This response wrapper is used to return an error. It contains the following fields:

| Name     | Type                     | Description                                                       |
| :------- | :----------------------- | :---------------------------------------------------------------- |
| Err      | `error`                  | The error that occurred                                           |
| Data     | `*T`                     | The deserialized error response data (nil if unmarshaling failed) |
| Body     | `[]byte`                 | The raw body of the API response                                  |
| Metadata | `OrdersSDKErrorMetadata` | Status code and headers returned by the API                       |

#### `OrdersSDKResponseMetadata`

This struct is shared by both response wrappers and contains the following fields:

| Name       | Type                | Description                                      |
| :--------- | :------------------ | :----------------------------------------------- |
| Headers    | `map[string]string` | A map containing the headers returned by the API |
| StatusCode | `int`               | The status code returned by the API              |

</details>

## Models

The SDK includes several models that represent the data structures used in API requests and responses. These models help in organizing and managing the data efficiently.

<details>
<summary>Below is a list of all available models with links to their detailed documentation:</summary>

| Name                                                               | Description          |
| :----------------------------------------------------------------- | :------------------- |
| [HealthResponse](documentation/models/health_response.md)          |                      |
| [OpenAPIDocument](documentation/models/open_api_document.md)       | OpenAPI 3.0 document |
| [OrderListResponse](documentation/models/order_list_response.md)   |                      |
| [Order](documentation/models/order.md)                             |                      |
| [CreateOrderRequest](documentation/models/create_order_request.md) |                      |
| [UpdateOrderRequest](documentation/models/update_order_request.md) |                      |
| [PatchOrderRequest](documentation/models/patch_order_request.md)   |                      |

</details>
