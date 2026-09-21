package com.orderssdk.http.interceptors;

import com.orderssdk.logging.Logger;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp interceptor that logs HTTP requests and responses.
 *
 * <p>Logs request method, URL, and headers (with sensitive values redacted) at debug level.
 * Logs response status at debug level, and 4xx/5xx responses at error level.
 * Sensitive query-string parameters (e.g. api_key, access_token, presigned X-Amz-* params) are
 * redacted from the logged URL, mirroring the header redaction. Does nothing if the logger is silent.
 */
public final class LoggingInterceptor implements Interceptor {

  private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(
    Arrays.asList(
      "authorization",
      "www-authenticate",
      "x-api-key",
      "api-key",
      "apikey",
      "x-api-token",
      "x-auth-token",
      "auth-token",
      "proxy-authenticate",
      "proxy-authorization",
      "cookie",
      "set-cookie",
      "x-csrf-token",
      "x-xsrf-token",
      "x-session-token",
      "x-access-token"
    )
  );

  private static final Set<String> SENSITIVE_QUERY_PARAMS = new HashSet<>(
    Arrays.asList(
      "api_key",
      "apikey",
      "api-key",
      "access_token",
      "accesstoken",
      "access-token",
      "token",
      "auth_token",
      "auth-token",
      "refresh_token",
      "id_token",
      "client_secret",
      "client-secret",
      "secret",
      "password",
      "signature",
      "sig"
    )
  );

  private final Logger logger;

  public LoggingInterceptor(Logger logger) {
    this.logger = logger;
  }

  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();

    if (logger.isDebug()) {
      StringBuilder sb = new StringBuilder();
      sb
        .append("HTTP Request: ")
        .append(request.method())
        .append(" ")
        .append(sanitizeUrl(request.url()));
      sb.append(" headers={");
      boolean first = true;
      for (String name : request.headers().names()) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(name).append("=");
        if (SENSITIVE_HEADERS.contains(name.toLowerCase())) {
          sb.append("[REDACTED]");
        } else {
          sb.append(request.header(name));
        }
        first = false;
      }
      sb.append("}");
      sb.append(" has_body=").append(request.body() != null);
      logger.debug(sb.toString());
    }

    Response response = chain.proceed(request);

    if (logger.isDebug()) {
      StringBuilder sb = new StringBuilder();
      sb.append("HTTP Response: status=").append(response.code());
      sb.append(" url=").append(sanitizeUrl(response.request().url()));
      sb.append(" headers={");
      boolean first = true;
      for (String name : response.headers().names()) {
        if (!first) {
          sb.append(", ");
        }
        sb.append(name).append("=");
        if (SENSITIVE_HEADERS.contains(name.toLowerCase())) {
          sb.append("[REDACTED]");
        } else {
          sb.append(response.header(name));
        }
        first = false;
      }
      sb.append("}");
      logger.debug(sb.toString());
    }

    if (response.code() >= 400 && logger.isError()) {
      logger.error(
        "HTTP Error: status=" + response.code() + " url=" + sanitizeUrl(response.request().url())
      );
    }

    return response;
  }

  /**
   * Renders a URL for logging with sensitive query-parameter values redacted. Credentials are
   * commonly passed in the query string (e.g. {@code ?api_key=...}, {@code ?access_token=...},
   * presigned {@code X-Amz-*} parameters); logging the raw URL would leak them, so any parameter
   * whose name is sensitive has its value replaced with {@code [REDACTED]} while other parameters
   * are preserved for debugging context.
   */
  private static String sanitizeUrl(HttpUrl url) {
    Set<String> names = url.queryParameterNames();
    if (names.isEmpty()) {
      return url.toString();
    }
    // Rebuild the query manually so the [REDACTED] marker stays readable in logs; going through
    // HttpUrl.Builder.addQueryParameter would percent-encode it to %5BREDACTED%5D.
    StringBuilder query = new StringBuilder();
    for (String name : names) {
      boolean sensitive = isSensitiveQueryParam(name);
      List<String> values = url.queryParameterValues(name);
      for (String value : values) {
        if (query.length() > 0) {
          query.append("&");
        }
        query.append(name);
        if (value != null) {
          query.append("=").append(sensitive ? "[REDACTED]" : value);
        }
      }
    }
    String base = url.newBuilder().query(null).build().toString();
    return base + "?" + query;
  }

  private static boolean isSensitiveQueryParam(String name) {
    String lower = name.toLowerCase();
    // X-Amz-Signature / X-Amz-Credential / X-Amz-Security-Token and friends (S3 presigned URLs).
    return SENSITIVE_QUERY_PARAMS.contains(lower) || lower.startsWith("x-amz-");
  }
}
