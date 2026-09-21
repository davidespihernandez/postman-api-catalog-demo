package com.orderssdk.http.interceptors;

import com.orderssdk.config.RetryConfig;
import com.orderssdk.http.HttpMethod;
import java.io.IOException;
import lombok.AllArgsConstructor;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * OkHttp interceptor that automatically retries failed HTTP requests.
 * Supports configurable retry attempts, backoff delays, and specific status codes or exceptions to retry.
 */
@AllArgsConstructor
public class RetryInterceptor implements Interceptor {

  private RetryConfig config;

  /**
   * Intercepts an HTTP request and retries it according to the configured retry policy.
   * Implements exponential backoff between retry attempts.
   *
   * @param chain The OkHttp interceptor chain
   * @return The HTTP response from a successful attempt
   * @throws IOException if all retry attempts fail or a non-retryable exception occurs
   */
  @Override
  public Response intercept(Chain chain) throws IOException {
    Request request = chain.request();
    int tryCount = 0;

    Response response = null;
    while (tryCount <= config.getMaxRetries()) {
      if (response != null) {
        response.close();
      }

      try {
        response = chain.proceed(request);
        if (!isRetryable(response)) {
          return response;
        }
        tryCount++;
      } catch (IOException e) {
        if (
          !config.getExceptionsToRetry().contains(e.getClass()) ||
          tryCount == config.getMaxRetries()
        ) {
          throw e;
        }
        // Count this failed attempt so a persistently failing connection eventually
        // exhausts its retries instead of looping forever, and clear any prior response
        // so the delay uses exponential backoff rather than a stale rate-limit header.
        tryCount++;
        response = null;
      }

      // Retries exhausted: return the last response without a final, useless delay.
      if (tryCount > config.getMaxRetries()) {
        return response;
      }

      final int delay = calculateDelay(tryCount, response);
      try {
        Thread.sleep(delay);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new IOException("Thread interrupted while waiting for retry", e);
      }
    }

    return response;
  }

  /**
   * Calculates the delay before the next retry attempt. A server rate-limit timing header
   * (Retry-After / X-RateLimit-Reset) on the response, when present, overrides the computed
   * exponential backoff; otherwise exponential backoff (capped at max delay) is used.
   *
   * @param tryCount The current retry attempt number (1-indexed)
   * @param response The response that triggered the retry (may be null on an exception retry)
   * @return The delay in milliseconds, capped at the configured maximum delay
   */
  private int calculateDelay(int tryCount, Response response) {
    final Long headerDelay = retryAfterDelayMs(response);
    if (headerDelay != null) {
      return headerDelay.intValue();
    }
    final int delay = (int) (config.getInitialDelay() *
      Math.pow(config.getBackoffFactor(), tryCount - 1));
    return Math.min(delay, config.getMaxDelay());
  }

  /**
   * Returns the server-directed retry delay (in milliseconds) from rate-limit response
   * headers, honoring Retry-After (delta-seconds or HTTP-date) and, when absent,
   * X-RateLimit-Reset (epoch seconds), clamped to maxRetryAfterDelay. Returns null when no
   * usable header is present so the caller falls back to the computed exponential backoff.
   *
   * @param response The response that triggered the retry (may be null)
   * @return The delay in milliseconds, or null to use exponential backoff
   */
  private Long retryAfterDelayMs(Response response) {
    if (response == null) {
      return null;
    }
    final long maxCap = config.getMaxRetryAfterDelay();
    if (maxCap <= 0) {
      return null;
    }

    // retry-after-ms (milliseconds) is a non-standard but finer-grained hint some APIs send
    // (e.g. OpenAI); it takes precedence over the whole-second Retry-After.
    final String retryAfterMs = response.header("Retry-After-Ms");
    if (retryAfterMs != null && retryAfterMs.trim().matches("\\d+(\\.\\d+)?")) {
      return Math.min((long) Double.parseDouble(retryAfterMs.trim()), maxCap);
    }

    Double seconds = parseRetryAfter(response.header("Retry-After"));
    if (seconds == null) {
      final String reset = response.header("X-RateLimit-Reset");
      if (reset != null && !reset.trim().isEmpty()) {
        try {
          final long epoch = Long.parseLong(reset.trim());
          final double delta = epoch - (System.currentTimeMillis() / 1000.0);
          if (delta > 0) {
            seconds = delta;
          }
        } catch (NumberFormatException ignored) {
          // X-RateLimit-Reset was not a valid epoch value; fall back to backoff.
        }
      }
    }

    if (seconds == null) {
      return null;
    }

    final long ms = (long) Math.max(0.0, seconds * 1000.0);
    return Math.min(ms, maxCap);
  }

  /**
   * Parses a Retry-After header value: an integer/float number of seconds, or an HTTP-date
   * (a past date yields 0). Returns the delay in seconds, or null if empty/unparseable.
   *
   * @param value The raw Retry-After header value
   * @return The delay in seconds, or null
   */
  private Double parseRetryAfter(String value) {
    if (value == null) {
      return null;
    }
    final String trimmed = value.trim();
    if (trimmed.isEmpty()) {
      return null;
    }
    if (trimmed.matches("\\d+(\\.\\d+)?")) {
      return Double.parseDouble(trimmed);
    }
    try {
      final java.time.ZonedDateTime date = java.time.ZonedDateTime.parse(
        trimmed,
        java.time.format.DateTimeFormatter.RFC_1123_DATE_TIME
      );
      final double delta = (date.toInstant().toEpochMilli() - System.currentTimeMillis()) / 1000.0;
      return delta > 0 ? delta : 0.0;
    } catch (java.time.format.DateTimeParseException e) {
      return null;
    }
  }

  /**
   * Determines if a response should be retried based on status code and HTTP method.
   * By default, retries all 5xx server errors and specific 4xx client errors (408 Timeout, 429 Rate Limit).
   *
   * @param response The HTTP response to check
   * @return true if the response should be retried, false otherwise
   */
  private boolean isRetryable(Response response) {
    final int statusCode = response.code();
    final boolean isRetryableStatusCode = !config.getStatusCodesToRetry().isEmpty()
      ? config.getStatusCodesToRetry().contains(statusCode)
      : statusCode >= 500 || statusCode == 408 || statusCode == 429;

    final boolean isRetryableMethod = config
      .getHttpMethodsToRetry()
      .stream()
      .map((HttpMethod m) -> m.getMethod())
      .anyMatch(method -> method.equals(response.request().method()));

    return isRetryableStatusCode && isRetryableMethod;
  }
}
