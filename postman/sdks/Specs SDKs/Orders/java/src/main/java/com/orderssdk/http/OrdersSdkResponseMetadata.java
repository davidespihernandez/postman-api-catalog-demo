package com.orderssdk.http;

import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * Metadata about an HTTP response including status code and headers.
 * Provides structured access to response information without the body content.
 */
@AllArgsConstructor
@Getter
@ToString
public class OrdersSdkResponseMetadata {

  /** The HTTP status code (e.g., 200, 404, 500) */
  private int statusCode;
  /** The response headers as a map of header names to lists of values */
  private Map<String, List<String>> headers;
}
