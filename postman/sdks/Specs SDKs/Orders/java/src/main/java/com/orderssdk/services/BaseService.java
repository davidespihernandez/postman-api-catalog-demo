package com.orderssdk.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orderssdk.config.OrdersSdkConfig;
import com.orderssdk.config.RequestConfig;
import com.orderssdk.exceptions.ApiError;
import com.orderssdk.exceptions.ConflictError;
import com.orderssdk.exceptions.ForbiddenError;
import com.orderssdk.exceptions.MethodNotAllowedError;
import com.orderssdk.exceptions.TooManyRequestsError;
import com.orderssdk.exceptions.UnauthorizedError;
import com.orderssdk.http.Environment;
import com.orderssdk.http.ModelConverter;
import com.orderssdk.http.interceptors.RetryInterceptor;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jetbrains.annotations.NotNull;

/**
 * Base service class that all API service classes extend.
 * Provides common functionality including HTTP request execution, error handling,
 * and configuration management.
 */
public class BaseService {

  private static final Logger logger = Logger.getLogger(BaseService.class.getName());
  protected OkHttpClient httpClient;
  protected OrdersSdkConfig config;
  protected Map<Integer, ErrorMapping<?>> errorMappings;
  protected ErrorMapping<?> defaultErrorMapping;

  /** Service-level configuration overrides */
  protected RequestConfig serviceConfig;

  /**
   * Factory that builds the status-named {@link ApiError} for a mapped status code.
   * Registered per status so exceptions are constructed with a typed body without reflection.
   */
  @FunctionalInterface
  public interface ApiErrorFactory {
    ApiError create(String message, int statusCode, Object body, Map<String, List<String>> headers);
  }

  /**
   * Internal class for mapping HTTP status codes to error models and exception factories.
   *
   * @param <T> The error model type
   */
  private static class ErrorMapping<T> {

    private final Class<T> modelClass;
    private final ApiErrorFactory factory;

    public ErrorMapping(Class<T> modelClass, ApiErrorFactory factory) {
      this.modelClass = modelClass;
      this.factory = factory;
    }
  }

  /**
   * Constructs a new BaseService instance.
   *
   * @param httpClient The HTTP client to use for making requests
   * @param config The SDK configuration
   */
  public BaseService(OkHttpClient httpClient, OrdersSdkConfig config) {
    this.httpClient = httpClient;
    this.config = config;
    this.errorMappings = new HashMap<>();
    // Seed the standard status-named exceptions (FSM-640) so every endpoint throws
    // them regardless of whether the spec declared that status. Per-method mappings
    // override these for statuses the spec models with a typed body.
    this.addErrorMapping(401, Object.class, (message, code, body, headers) ->
        new UnauthorizedError(message, body, headers)
      );
    this.addErrorMapping(403, Object.class, (message, code, body, headers) ->
        new ForbiddenError(message, body, headers)
      );
    this.addErrorMapping(405, Object.class, (message, code, body, headers) ->
        new MethodNotAllowedError(message, body, headers)
      );
    this.addErrorMapping(409, Object.class, (message, code, body, headers) ->
        new ConflictError(message, body, headers)
      );
    this.addErrorMapping(429, Object.class, (message, code, body, headers) ->
        new TooManyRequestsError(message, body, headers)
      );
  }

  /**
   * Sets the base URL for API requests.
   *
   * @param baseUrl The base URL to use
   */
  public void setBaseUrl(String baseUrl) {
    this.config.setBaseUrl(baseUrl);
  }

  /**
   * Sets the environment for API requests.
   *
   * @param environment The environment to use (e.g., DEFAULT, PRODUCTION, STAGING)
   */
  public void setEnvironment(Environment environment) {
    this.config.setEnvironment(environment);
  }

  /**
   * Sets service-level configuration that applies to all methods in this service.
   * Service-level overrides take precedence over SDK-level configuration but are
   * overridden by method-level and request-level configurations.
   *
   * @param config The configuration overrides to apply at the service level
   */
  public void setConfig(RequestConfig config) {
    this.serviceConfig = config;
  }

  /**
   * Resolves configuration from the hierarchy: requestConfig > methodConfig > serviceConfig.
   * Merges override configs into a single {@link RequestConfig}. SDK defaults are used as
   * fallbacks where these overrides are not provided.
   *
   * @param methodConfig Method-level configuration override (may be null)
   * @param requestConfig Request-level configuration override (may be null)
   * @return Merged configuration with all overrides applied
   */
  protected RequestConfig getResolvedConfig(
    RequestConfig methodConfig,
    RequestConfig requestConfig
  ) {
    return RequestConfig.merge(this.serviceConfig, methodConfig, requestConfig);
  }

  /**
   * Resolves the base URL from the configuration hierarchy.
   * Priority: resolvedConfig.baseUrl > sdkConfig.baseUrl > resolvedConfig.environment > defaultEnvironment.
   *
   * @param resolvedConfig The merged request configuration (may be null)
   * @param defaultEnvironment The default environment for this method
   * @return The resolved base URL
   */
  protected String resolveBaseUrl(RequestConfig resolvedConfig, Environment defaultEnvironment) {
    if (resolvedConfig != null && resolvedConfig.getBaseUrl() != null) {
      return resolvedConfig.getBaseUrl();
    }
    if (this.config.getBaseUrl() != null) {
      return this.config.getBaseUrl();
    }
    if (resolvedConfig != null && resolvedConfig.getEnvironment() != null) {
      return resolvedConfig.getEnvironment().getUrl();
    }
    return defaultEnvironment.getUrl();
  }

  /**
   * Creates an OkHttpClient with per-request overrides from the resolved configuration.
   * Applies timeout and/or retry config overrides when present.
   * If no overrides are present, returns the original client.
   *
   * @param resolvedConfig The resolved request configuration
   * @return An OkHttpClient with the appropriate overrides applied
   */
  protected OkHttpClient getHttpClientForRequest(RequestConfig resolvedConfig) {
    if (resolvedConfig == null) {
      return this.httpClient;
    }

    boolean needsRebuild = resolvedConfig.getTimeout() != null;
    needsRebuild = needsRebuild || resolvedConfig.getRetryConfig() != null;

    if (!needsRebuild) {
      return this.httpClient;
    }

    OkHttpClient.Builder builder = this.httpClient.newBuilder();

    if (resolvedConfig.getTimeout() != null) {
      builder.readTimeout(resolvedConfig.getTimeout(), TimeUnit.MILLISECONDS);
    }

    if (resolvedConfig.getRetryConfig() != null) {
      List<okhttp3.Interceptor> interceptors = new ArrayList<>(builder.interceptors());
      interceptors.replaceAll(i ->
        i instanceof RetryInterceptor ? new RetryInterceptor(resolvedConfig.getRetryConfig()) : i
      );
      builder.interceptors().clear();
      interceptors.forEach(builder::addInterceptor);
    }

    return builder.build();
  }

  /**
   * Registers an error mapping for a specific HTTP status code.
   * When a response with this status is received, the SDK will deserialize the error
   * response to the specified model class and throw the specified exception type.
   *
   * @param <T> The error model type
   * @param status The HTTP status code to map
   * @param modelClass The class to deserialize the error response into
   * @param factory The factory that builds the exception to throw
   */
  protected <T> void addErrorMapping(int status, Class<T> modelClass, ApiErrorFactory factory) {
    this.errorMappings.put(status, new ErrorMapping<>(modelClass, factory));
  }

  /**
   * Registers a default error mapping for unmapped HTTP status codes.
   * When a response with an unmapped status is received the SDK will deserialize the error
   * response to the specified model class and build the exception via the given factory.
   *
   * @param <T> The error model type
   * @param modelClass The class to deserialize the error response into
   * @param factory The factory that builds the exception to throw
   */
  protected <T> void addDefaultErrorMapping(Class<T> modelClass, ApiErrorFactory factory) {
    this.defaultErrorMapping = new ErrorMapping<>(modelClass, factory);
  }

  /**
   * Extracts an error message from a response and optional error model.
   * Attempts to get the message from the error model's getMessage() method,
   * falls back to the response message, or constructs a message from the status code and URL.
   *
   * @param response The HTTP response
   * @param errorModel The deserialized error model (may be null)
   * @return The extracted or constructed error message
   */
  private String extractErrorMessage(Response response, Object errorModel) {
    String message = null;

    if (errorModel != null) {
      try {
        message = (String) errorModel.getClass().getMethod("getMessage").invoke(errorModel);
      } catch (Exception e) {
        // Ignore if getMessage doesn't exist or fails
      }
    }

    if (Objects.isNull(message) || message.trim().isEmpty()) {
      message = response.message();
    }

    if (Objects.isNull(message) || message.trim().isEmpty()) {
      message = String.format(
        "%d error in request to: %s",
        response.code(),
        response.request().url()
      );
    }

    return message;
  }

  /**
   * Executes an HTTP request synchronously.
   * Handles error responses by checking error mappings and throwing appropriate exceptions.
   *
   * @param request The HTTP request to execute
   * @param resolvedConfig The resolved request configuration (may contain timeout override)
   * @return The HTTP response if successful
   * @throws ApiError if the request fails or returns an error status code
   */
  protected Response execute(Request request, RequestConfig resolvedConfig) throws ApiError {
    OkHttpClient client = this.getHttpClientForRequest(resolvedConfig);
    Response response;
    try {
      response = client.newCall(request).execute();
    } catch (IOException e) {
      if (e instanceof SocketTimeoutException) {
        throw new ApiError("Request timed out", 408, null, Collections.emptyMap());
      }
      throw new ApiError(e.getMessage(), 0, null, Collections.emptyMap());
    }

    if (response.isSuccessful()) {
      return response;
    }

    // Handle error response
    Map<String, List<String>> headers = response.headers().toMultimap();
    ErrorMapping<?> errorMapping =
      this.errorMappings.getOrDefault(response.code(), this.defaultErrorMapping);
    if (errorMapping != null) {
      Object errorModel = null;
      try {
        errorModel = ModelConverter.convert(response, errorMapping.modelClass);
      } catch (Exception e) {
        logger.log(
          Level.WARNING,
          "Failed to deserialize error response to " + errorMapping.modelClass.getName(),
          e
        );
      }

      // A mapped status always throws its status-named exception, even when the body is
      // absent or fails to deserialize (errorModel is then null). Only a status with no
      // mapping at all falls through to the base ApiError below.
      String message = extractErrorMessage(response, errorModel);
      throw errorMapping.factory.create(message, response.code(), errorModel, headers);
    }

    // No mapping (and no default) for this status: throw the base ApiError with parsed headers.
    throw new ApiError(extractErrorMessage(response, null), response.code(), null, headers);
  }

  /**
   * Executes an HTTP request asynchronously.
   * Returns a CompletableFuture that completes with the response or completes exceptionally
   * if an error occurs. Handles error responses by checking error mappings.
   *
   * @param request The HTTP request to execute
   * @param resolvedConfig The resolved request configuration (may contain timeout override)
   * @return A CompletableFuture that completes with the HTTP response
   */
  protected CompletableFuture<Response> executeAsync(
    Request request,
    RequestConfig resolvedConfig
  ) {
    OkHttpClient client = this.getHttpClientForRequest(resolvedConfig);
    CompletableFuture<Response> future = new CompletableFuture<>();
    client
      .newCall(request)
      .enqueue(
        new Callback() {
          @Override
          public void onResponse(@NotNull Call call, @NotNull Response response) {
            if (!response.isSuccessful()) {
              // Handle error response
              Map<String, List<String>> headers = response.headers().toMultimap();
              ErrorMapping<?> errorMapping = errorMappings.getOrDefault(
                response.code(),
                defaultErrorMapping
              );
              if (errorMapping != null) {
                Object errorModel = null;
                try {
                  errorModel = ModelConverter.convert(response, errorMapping.modelClass);
                } catch (Exception e) {
                  logger.log(
                    Level.WARNING,
                    "Failed to deserialize error response to " + errorMapping.modelClass.getName(),
                    e
                  );
                }
                // A mapped status always throws its status-named exception, even when the
                // body is absent or fails to deserialize (errorModel is then null).
                String message = extractErrorMessage(response, errorModel);
                future.completeExceptionally(
                  errorMapping.factory.create(message, response.code(), errorModel, headers)
                );
                return;
              }

              // No mapping (and no default) for this status: complete with the base ApiError.
              ApiError error = new ApiError(
                extractErrorMessage(response, null),
                response.code(),
                null,
                headers
              );
              future.completeExceptionally(error);
              return;
            }

            future.complete(response);
          }

          @Override
          public void onFailure(@NotNull Call call, @NotNull IOException e) {
            if (e instanceof SocketTimeoutException) {
              ApiError error = new ApiError("Request timed out", 408, null, Collections.emptyMap());
              future.completeExceptionally(error);
            } else {
              ApiError error = new ApiError(e.getMessage(), 0, null, Collections.emptyMap());
              future.completeExceptionally(error);
            }
          }
        }
      );
    return future;
  }
}
