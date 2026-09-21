# OrdersSdk TypeScript SDK 1.0.0

Welcome to the OrdersSdk SDK documentation. This guide will help you get started with integrating and using the OrdersSdk SDK in your project.

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
  - [Installation](#installation)
- [Setting a Custom Timeout](#setting-a-custom-timeout)
- [Sample Usage](#sample-usage)
- [Services](#services)
- [Models](#models)

# Setup & Configuration

## Supported Language Versions

This SDK is compatible with the following versions: `TypeScript >= 4.8.4`

## Installation

To get started with the SDK, we recommend installing using `npm` or `yarn`:

```bash
npm install orders-sdk
```

or

```bash
yarn add orders-sdk
```

## Setting a Custom Timeout

You can set a custom timeout for the SDK's HTTP requests as follows:

```ts
const ordersSdk = new OrdersSdk({ timeout: 10000 });
```

# Sample Usage

Below is a comprehensive example demonstrating how to authenticate and call a simple endpoint:

```ts
import { OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const data = await ordersSdk.system.getHealth();

  console.log(data);
})();
```

## Services

The SDK provides various services to interact with the API.

<details>
<summary>Below is a list of all available services with links to their detailed documentation:</summary>

| Name                                                     |
| :------------------------------------------------------- |
| [SystemService](documentation/services/SystemService.md) |
| [OrdersService](documentation/services/OrdersService.md) |

</details>

## Models

The SDK includes several models that represent the data structures used in API requests and responses. These models help in organizing and managing the data efficiently.

<details>
<summary>Below is a list of all available models with links to their detailed documentation:</summary>

| Name                                                               | Description          |
| :----------------------------------------------------------------- | :------------------- |
| [HealthResponse](documentation/models/HealthResponse.md)           |                      |
| [ServerErrorResponse](documentation/models/ServerErrorResponse.md) |                      |
| [OpenApiDocument](documentation/models/OpenApiDocument.md)         | OpenAPI 3.0 document |
| [OrderListResponse](documentation/models/OrderListResponse.md)     |                      |
| [Order](documentation/models/Order.md)                             |                      |
| [ServerErrorResponse](documentation/models/ServerErrorResponse.md) |                      |
| [CreateOrderRequest](documentation/models/CreateOrderRequest.md)   |                      |
| [ErrorResponse](documentation/models/ErrorResponse.md)             |                      |
| [UpdateOrderRequest](documentation/models/UpdateOrderRequest.md)   |                      |
| [PatchOrderRequest](documentation/models/PatchOrderRequest.md)     |                      |

</details>
