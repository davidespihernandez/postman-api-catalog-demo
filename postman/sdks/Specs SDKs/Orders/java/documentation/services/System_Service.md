# System_Service

A list of all methods in the `System_Service` service. Click on the method name to view detailed information about that method.

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

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.models.HealthResponse;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    HealthResponse response = ordersSdk.system_.getHealth();

    System.out.println(response);
  }
}

```

## getOpenApi

- HTTP Method: `GET`
- Endpoint: `/openapi.json`

**Return Type**

`OpenApiDocument`

**Example Usage Code Snippet**

```java
import com.orderssdk.OrdersSdk;
import com.orderssdk.models.OpenApiDocument;

public class Main {

  public static void main(String[] args) {
    OrdersSdk ordersSdk = new OrdersSdk();

    OpenApiDocument response = ordersSdk.system_.getOpenApi();

    System.out.println(response);
  }
}

```
