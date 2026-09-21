package com.orderssdk.config;

import com.orderssdk.http.Environment;
import com.orderssdk.logging.LogConfig;
import lombok.Builder;
import lombok.Data;
import lombok.NonNull;
import lombok.Setter;
import okhttp3.OkHttpClient;

/**
 * Configuration class for SDK client settings.
 * Provides builder pattern for configuring base URLs, authentication, timeouts, and retry behavior.
 * All configuration options have sensible defaults and can be customized as needed.
 */
@Builder
@Data
public class OrdersSdkConfig {

  @NonNull
  @Builder.Default
  private String userAgent = "postman-codegen/2.7.0 orderssdk/1.0.0 (java)";

  @Setter
  private String baseUrl;

  @NonNull
  @Builder.Default
  private RetryConfig retryConfig = RetryConfig.builder().build();

  /** Timeout in milliseconds */
  @Builder.Default
  private long timeout = 10_000;

  /**
   * Optional user-supplied OkHttpClient. When set, the SDK derives its client from this
   * instance (via {@code newBuilder()}) and layers its own interceptors on top, so custom
   * transport settings (proxy, connection pool, TLS, timeouts, interceptors) are preserved
   * while SDK behavior (auth, retry, etc.) still applies. When null, the SDK builds a default client.
   * <p>Note: the config-level {@code timeout} is not applied to an injected client (it keeps its
   * own timeout settings); per-request, method, and service-level timeout overrides still apply.
   */
  private OkHttpClient httpClient;

  /**
   * Optional logging configuration. Logging is opt-in and silent by default: the SDK ships a
   * pluggable logging facility (see {@code LogConfig}, {@code ILogger}, {@code ConsoleLogger},
   * {@code LogLevel}) that emits request/response diagnostics only once a non-silent
   * {@code LogConfig} is supplied. Left at its default, the SDK produces no log output.
   */
  @NonNull
  @Builder.Default
  private LogConfig logConfig = LogConfig.builder().build();

  public void setEnvironment(Environment environment) {
    this.baseUrl = environment.getUrl();
  }
}
