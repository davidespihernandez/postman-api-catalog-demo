package com.orderssdk;

import com.orderssdk.config.OrdersSdkConfig;
import com.orderssdk.http.Environment;
import com.orderssdk.http.interceptors.DefaultHeadersInterceptor;
import com.orderssdk.http.interceptors.LoggingInterceptor;
import com.orderssdk.http.interceptors.RetryInterceptor;
import com.orderssdk.logging.Logger;
import com.orderssdk.services.OrdersService;
import com.orderssdk.services.System_Service;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;

/**
 * Demo orders service for the Postman API Catalog walkthrough.
 *
 * Optional query parameters on all endpoints except /openapi.json:
 * - delay or wait: milliseconds to wait before responding (max 30000)
 * - status or error: HTTP status code to return (e.g. 500); error=true also returns 500
 */
public class OrdersSdk {

  public final System_Service system_;
  public final OrdersService orders;

  private final OrdersSdkConfig config;

  /**
   * Constructs a new instance of OrdersSdk with default configuration.
   */
  public OrdersSdk() {
    // Default configs
    this(OrdersSdkConfig.builder().build());
  }

  /**
   * Constructs a new instance of OrdersSdk with custom configuration.
   * Initializes all services, HTTP client, and optional OAuth token manager.
   *
   * @param config The SDK configuration including base URL, authentication, timeout, and retry settings
   */
  public OrdersSdk(OrdersSdkConfig config) {
    this.config = config;

    // A user-supplied client is augmented (not replaced): the SDK derives its client from
    // the injected instance so its transport settings and interceptors are preserved, then
    // layers the SDK's own interceptors on top.
    final OkHttpClient customHttpClient = config.getHttpClient();
    final OkHttpClient.Builder httpClientBuilder =
      (customHttpClient != null
          ? customHttpClient.newBuilder()
          : new OkHttpClient.Builder()).addInterceptor(new DefaultHeadersInterceptor(config))
        .addInterceptor(new RetryInterceptor(config.getRetryConfig()))
        // Logging is added last so it observes the fully-decorated request (auth headers
        // included, then redacted). Silent by default — see LogConfig.
        .addInterceptor(new LoggingInterceptor(Logger.from(config.getLogConfig())));

    // Only apply the SDK's default read timeout when building the client ourselves; a
    // user-supplied client owns its own transport (timeout) settings.
    if (customHttpClient == null) {
      httpClientBuilder.readTimeout(config.getTimeout(), TimeUnit.MILLISECONDS);
    }

    final OkHttpClient httpClient = httpClientBuilder.build();

    this.system_ = new System_Service(httpClient, config);
    this.orders = new OrdersService(httpClient, config);
  }

  /**
   * Sets the environment for all API requests.
   *
   * @param environment The environment to use (e.g., DEFAULT, PRODUCTION, STAGING)
   */
  public void setEnvironment(Environment environment) {
    setBaseUrl(environment.getUrl());
  }

  /**
   * Sets the base URL for all API requests.
   *
   * @param baseUrl The base URL to use for API requests
   */
  public void setBaseUrl(String baseUrl) {
    this.config.setBaseUrl(baseUrl);
  }
}
// c029837e0e474b76bc487506e8799df5e3335891efe4fb02bda7a1441840310c
