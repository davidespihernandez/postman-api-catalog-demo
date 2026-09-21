package com.orderssdk.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Initial order status
 */
public final class CreateOrderRequestStatus {

  public static final CreateOrderRequestStatus PENDING = new CreateOrderRequestStatus(
    Value.PENDING,
    "pending"
  );
  public static final CreateOrderRequestStatus PROCESSING = new CreateOrderRequestStatus(
    Value.PROCESSING,
    "processing"
  );
  public static final CreateOrderRequestStatus SHIPPED = new CreateOrderRequestStatus(
    Value.SHIPPED,
    "shipped"
  );
  public static final CreateOrderRequestStatus CANCELLED = new CreateOrderRequestStatus(
    Value.CANCELLED,
    "cancelled"
  );

  private final Value value;

  private final String string;

  CreateOrderRequestStatus(Value value, String string) {
    this.value = value;
    this.string = string;
  }

  public Value getEnumValue() {
    return value;
  }

  @java.lang.Override
  @JsonValue
  public String toString() {
    return this.string;
  }

  @java.lang.Override
  public boolean equals(Object other) {
    return (
      (this == other) ||
      (other instanceof CreateOrderRequestStatus &&
        this.string.equals(((CreateOrderRequestStatus) other).string))
    );
  }

  @java.lang.Override
  public int hashCode() {
    return this.string.hashCode();
  }

  public <T> T visit(Visitor<T> visitor) {
    switch (value) {
      case PENDING:
        return visitor.visitPending();
      case PROCESSING:
        return visitor.visitProcessing();
      case SHIPPED:
        return visitor.visitShipped();
      case CANCELLED:
        return visitor.visitCancelled();
      case UNKNOWN:
      default:
        return visitor.visitUnknown(string);
    }
  }

  @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
  public static CreateOrderRequestStatus valueOf(String value) {
    if (value == null) {
      return null;
    }
    switch (value) {
      case "pending":
        return PENDING;
      case "processing":
        return PROCESSING;
      case "shipped":
        return SHIPPED;
      case "cancelled":
        return CANCELLED;
      default:
        return new CreateOrderRequestStatus(Value.UNKNOWN, value);
    }
  }

  public enum Value {
    PENDING,
    PROCESSING,
    SHIPPED,
    CANCELLED,
    UNKNOWN,
  }

  public interface Visitor<T> {
    T visitPending();
    T visitProcessing();
    T visitShipped();
    T visitCancelled();
    T visitUnknown(String unknownType);
  }
}
