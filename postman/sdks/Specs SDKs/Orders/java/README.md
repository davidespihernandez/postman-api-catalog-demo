# OrdersSdk Java SDK 1.0.0

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
- [Injecting a Custom HTTP Client](#injecting-a-custom-http-client)
- [Accessing the Raw HTTP Response](#accessing-the-raw-http-response)
- [Sample Usage](#sample-usage)
- [Services](#services)
- [Models](#models)

# Setup & Configuration

## Supported Language Versions

This SDK is compatible with the following versions: `Java >= 1.8`

## Installation

If you use Maven, place the following within the _dependency_ tag in your `pom.xml` file:

```XML
<dependency>
    <groupId>com</groupId>
    <artifactId>orderssdk</artifactId>
    <version>1.0.0</version>
</dependency>
```

If you use Gradle, paste the next line inside the _dependencies_ block of your `build.gradle` file:

```Gradle
implementation("com:orderssdk:1.0.0")
```

If you use JAR files, package the SDK by running the following command:

```shell
mvn compile assembly:single
```

Then, add the JAR file to your project's classpath.

## Setting a Custom Timeout

You can set a custom timeout for the SDK's HTTP requests as follows:

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.config.OrdersSdkConfig;

public class Main {

  public static void main(String[] args) {
    OrdersSdkConfig config = OrdersSdkConfig.builder().timeout(10000).build();
    OrdersSdk ordersSdk = new OrdersSdk(config);
  }
}

```

## Injecting a Custom HTTP Client

You can supply your own `OkHttpClient` — for example to configure a proxy, a shared connection pool, custom TLS, timeouts, or your own interceptors. The SDK derives its client from the one you provide (preserving your transport settings and interceptors) and layers its own interceptors (such as authentication and retry) on top, so the SDK keeps working as usual.

```java
OkHttpClient customClient = new OkHttpClient.Builder().addInterceptor(new MyInterceptor()).build();

OrdersSdk ordersSdk = new OrdersSdk(OrdersSdkConfig.builder().httpClient(customClient).build());

```

`MyInterceptor` above is a placeholder for your own `okhttp3.Interceptor`.

> Your client's interceptors are added ahead of the SDK's, so on the outbound request they run before the SDK adds its own headers. A logging interceptor placed this way will **not** see SDK-injected headers such as authentication.

> **Timeout precedence:** when you inject a client, the config-level `timeout` is not applied — your client's own timeout settings are preserved. Per-request, method, and service-level timeout overrides still apply, layered on top of your client.

## Accessing the Raw HTTP Response

Every service method returns the parsed response body by default. When you also need the status code, response headers, or the raw HTTP response, call the same method through the per-call `withRawResponse()` accessor. The default methods are unchanged, so this is fully opt-in.

```java
OrdersSdkResponse<HealthResponse> response =
    ordersSdk.system_.withRawResponse().getHealth();

response.getData();
response.getMetadata().getStatusCode();
response.getMetadata().getHeaders();
response.getRaw();
```

`getData()` returns the same value the default method would; `getMetadata()` exposes the status code and headers, and `getRaw()` exposes the underlying HTTP response.

# Sample Usage

Below is a comprehensive example demonstrating how to authenticate and call a simple endpoint:

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.exceptions.ApiError;
import com.orderssdk.models.HealthResponse;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    try {
      HealthResponse response = ordersSdk.system_.getHealth();

      System.out.println(response);
    } catch (ApiError e) {
      e.printStackTrace();
    }

    System.exit(0);
  }
}

```

## Services

The SDK provides various services to interact with the API.

<details>
<summary>Below is a list of all available services with links to their detailed documentation:</summary>

| Name                                                       |
| :--------------------------------------------------------- |
| [System_Service](documentation/services/System_Service.md) |
| [OrdersService](documentation/services/OrdersService.md)   |

</details>

## Models

The SDK includes several models that represent the data structures used in API requests and responses. These models help in organizing and managing the data efficiently.

<details>
<summary>Below is a list of all available models with links to their detailed documentation:</summary>

| Name                                                               | Description          |
| :----------------------------------------------------------------- | :------------------- |
| [HealthResponse](documentation/models/HealthResponse.md)           |                      |
| [OpenApiDocument](documentation/models/OpenApiDocument.md)         | OpenAPI 3.0 document |
| [OrderListResponse](documentation/models/OrderListResponse.md)     |                      |
| [Order](documentation/models/Order.md)                             |                      |
| [CreateOrderRequest](documentation/models/CreateOrderRequest.md)   |                      |
| [UpdateOrderRequest](documentation/models/UpdateOrderRequest.md)   |                      |
| [PatchOrderRequest](documentation/models/PatchOrderRequest.md)     |                      |
| [ServerErrorResponse](documentation/models/ServerErrorResponse.md) |                      |
| [ErrorResponse](documentation/models/ErrorResponse.md)             |                      |

</details>
