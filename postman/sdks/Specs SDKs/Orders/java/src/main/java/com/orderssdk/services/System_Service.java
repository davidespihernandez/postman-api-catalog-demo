package com.orderssdk.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.orderssdk.config.OrdersSdkConfig;
import com.orderssdk.config.RequestConfig;
import com.orderssdk.exceptions.ApiError;
import com.orderssdk.exceptions.InternalServerError;
import com.orderssdk.http.Environment;
import com.orderssdk.http.HttpMethod;
import com.orderssdk.http.ModelConverter;
import com.orderssdk.http.OrdersSdkResponse;
import com.orderssdk.http.util.RequestBuilder;
import com.orderssdk.models.HealthResponse;
import com.orderssdk.models.OpenApiDocument;
import com.orderssdk.models.ServerErrorResponse;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * System_Service Service
 */
public class System_Service extends BaseService {

  private RequestConfig getHealthConfig;
  private RequestConfig getOpenApiConfig;

  /**
   * Constructs a new instance of System_Service.
   *
   * @param httpClient The HTTP client to use for requests
   * @param config The SDK configuration
   */
  public System_Service(@NonNull OkHttpClient httpClient, OrdersSdkConfig config) {
    super(httpClient, config);
  }

  /**
   * Sets method-level configuration for {@code getHealth}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public System_Service setGetHealthConfig(RequestConfig config) {
    this.getHealthConfig = config;
    return this;
  }

  /**
   * Sets method-level configuration for {@code getOpenApi}.
   * Method-level overrides take precedence over service-level configuration but are
   * overridden by request-level configurations.
   *
   * @param config The configuration overrides to apply at the method level
   * @return This service instance for method chaining
   */
  public System_Service setGetOpenApiConfig(RequestConfig config) {
    this.getOpenApiConfig = config;
    return this;
  }

  /**
   * Health check
   *
   * @return response of {@code HealthResponse}
   */
  public HealthResponse getHealth() throws ApiError {
    return this.getHealth(null);
  }

  /**
   * Health check
   *
   * @return response of {@code HealthResponse}
   */
  public HealthResponse getHealth(RequestConfig requestConfig) throws ApiError {
    return withRawResponse().getHealth(requestConfig).getData();
  }

  /**
   * Health check
   *
   * @return response of {@code CompletableFuture<HealthResponse>}
   */
  public CompletableFuture<HealthResponse> getHealthAsync() throws ApiError {
    return this.getHealthAsync(null);
  }

  /**
   * Health check
   *
   * @return response of {@code CompletableFuture<HealthResponse>}
   */
  public CompletableFuture<HealthResponse> getHealthAsync(RequestConfig requestConfig)
    throws ApiError {
    return withRawResponse()
      .getHealthAsync(requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildGetHealthRequest(RequestConfig resolvedConfig) {
    return new RequestBuilder(
      HttpMethod.GET,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "health"
    ).build();
  }

  /**
   * OpenAPI specification
   *
   * @return response of {@code OpenApiDocument}
   */
  public OpenApiDocument getOpenApi() throws ApiError {
    return this.getOpenApi(null);
  }

  /**
   * OpenAPI specification
   *
   * @return response of {@code OpenApiDocument}
   */
  public OpenApiDocument getOpenApi(RequestConfig requestConfig) throws ApiError {
    return withRawResponse().getOpenApi(requestConfig).getData();
  }

  /**
   * OpenAPI specification
   *
   * @return response of {@code CompletableFuture<OpenApiDocument>}
   */
  public CompletableFuture<OpenApiDocument> getOpenApiAsync() throws ApiError {
    return this.getOpenApiAsync(null);
  }

  /**
   * OpenAPI specification
   *
   * @return response of {@code CompletableFuture<OpenApiDocument>}
   */
  public CompletableFuture<OpenApiDocument> getOpenApiAsync(RequestConfig requestConfig)
    throws ApiError {
    return withRawResponse()
      .getOpenApiAsync(requestConfig)
      .thenApply(response -> response.getData());
  }

  private Request buildGetOpenApiRequest(RequestConfig resolvedConfig) {
    return new RequestBuilder(
      HttpMethod.GET,
      resolveBaseUrl(resolvedConfig, Environment.DEFAULT),
      "openapi.json"
    ).build();
  }

  /**
   * Returns an accessor whose methods mirror this service but return the full HTTP response
   * (status code, headers, and raw body) wrapped alongside the parsed data.
   *
   * @return An accessor exposing raw-response variants of this service's methods
   */
  public WithRawResponse withRawResponse() {
    return new WithRawResponse();
  }

  /**
   * Per-call accessor exposing raw-response variants of {@link System_Service}'s methods.
   * Reuses the enclosing service's request builders and configuration.
   */
  public class WithRawResponse {

    /**
     * Health check
     *
     * @return response of {@code OrdersSdkResponse<HealthResponse>}
     */
    public OrdersSdkResponse<HealthResponse> getHealth() throws ApiError {
      return this.getHealth(null);
    }

    /**
     * Health check
     *
     * @return response of {@code OrdersSdkResponse<HealthResponse>}
     */
    public OrdersSdkResponse<HealthResponse> getHealth(RequestConfig requestConfig)
      throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(getHealthConfig, requestConfig);
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildGetHealthRequest(resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<>(
        response,
        bodyBytes,
        ModelConverter.convert(bodyBytes, new TypeReference<HealthResponse>() {})
      );
    }

    /**
     * Health check
     *
     * @return response of {@code CompletableFuture<OrdersSdkResponse<HealthResponse>>}
     */
    public CompletableFuture<OrdersSdkResponse<HealthResponse>> getHealthAsync() throws ApiError {
      return this.getHealthAsync(null);
    }

    /**
     * Health check
     *
     * @return response of {@code CompletableFuture<OrdersSdkResponse<HealthResponse>>}
     */
    public CompletableFuture<OrdersSdkResponse<HealthResponse>> getHealthAsync(
      RequestConfig requestConfig
    ) throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(getHealthConfig, requestConfig);
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildGetHealthRequest(resolvedConfig);
      CompletableFuture<Response> futureResponse = executeAsync(request, resolvedConfig);
      return futureResponse.thenApplyAsync(response -> {
        byte[] bodyBytes = ModelConverter.readBytes(response);
        return new OrdersSdkResponse<>(
          response,
          bodyBytes,
          ModelConverter.convert(bodyBytes, new TypeReference<HealthResponse>() {})
        );
      });
    }

    /**
     * OpenAPI specification
     *
     * @return response of {@code OrdersSdkResponse<OpenApiDocument>}
     */
    public OrdersSdkResponse<OpenApiDocument> getOpenApi() throws ApiError {
      return this.getOpenApi(null);
    }

    /**
     * OpenAPI specification
     *
     * @return response of {@code OrdersSdkResponse<OpenApiDocument>}
     */
    public OrdersSdkResponse<OpenApiDocument> getOpenApi(RequestConfig requestConfig)
      throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(getOpenApiConfig, requestConfig);
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildGetOpenApiRequest(resolvedConfig);
      Response response = execute(request, resolvedConfig);
      byte[] bodyBytes = ModelConverter.readBytes(response);
      return new OrdersSdkResponse<>(
        response,
        bodyBytes,
        ModelConverter.convert(bodyBytes, new TypeReference<OpenApiDocument>() {})
      );
    }

    /**
     * OpenAPI specification
     *
     * @return response of {@code CompletableFuture<OrdersSdkResponse<OpenApiDocument>>}
     */
    public CompletableFuture<OrdersSdkResponse<OpenApiDocument>> getOpenApiAsync() throws ApiError {
      return this.getOpenApiAsync(null);
    }

    /**
     * OpenAPI specification
     *
     * @return response of {@code CompletableFuture<OrdersSdkResponse<OpenApiDocument>>}
     */
    public CompletableFuture<OrdersSdkResponse<OpenApiDocument>> getOpenApiAsync(
      RequestConfig requestConfig
    ) throws ApiError {
      RequestConfig resolvedConfig = getResolvedConfig(getOpenApiConfig, requestConfig);
      addErrorMapping(500, ServerErrorResponse.class, (message, code, body, headers) ->
        new InternalServerError(message, (ServerErrorResponse) body, headers)
      );
      Request request = buildGetOpenApiRequest(resolvedConfig);
      CompletableFuture<Response> futureResponse = executeAsync(request, resolvedConfig);
      return futureResponse.thenApplyAsync(response -> {
        byte[] bodyBytes = ModelConverter.readBytes(response);
        return new OrdersSdkResponse<>(
          response,
          bodyBytes,
          ModelConverter.convert(bodyBytes, new TypeReference<OpenApiDocument>() {})
        );
      });
    }
  }
}
