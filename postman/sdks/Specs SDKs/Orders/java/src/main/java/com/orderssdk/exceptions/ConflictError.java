package com.orderssdk.exceptions;

import java.util.List;
import java.util.Map;

/**
 * Error thrown for HTTP 409 responses.
 *
 * Part of the status-named exception hierarchy; extends {@link ApiError} and
 * carries the raw error body via {@code body()} (untyped for this status).
 */
public class ConflictError extends ApiError {

  public ConflictError(String message, Object body, Map<String, List<String>> headers) {
    super(message, 409, body, headers);
  }
}
