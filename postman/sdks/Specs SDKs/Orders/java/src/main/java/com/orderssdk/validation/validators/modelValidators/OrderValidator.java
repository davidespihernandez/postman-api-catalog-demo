package com.orderssdk.validation.validators.modelValidators;

import com.orderssdk.models.Order;
import com.orderssdk.validation.Violation;
import com.orderssdk.validation.ViolationAggregator;
import com.orderssdk.validation.validators.NumericValidator;
import com.orderssdk.validation.validators.StringValidator;

/**
 * Validator implementation for Order model.
 * Validates all fields and nested structures according to the model's constraints.
 */
public class OrderValidator extends AbstractModelValidator<Order> {

  /**
   * Creates a validator with a field name for nested validation paths.
   *
   * @param fieldName The field name to use in violation paths
   */
  public OrderValidator(String fieldName) {
    super(fieldName);
  }

  /**
   * Creates a validator for root-level validation.
   */
  public OrderValidator() {}

  /**
   * Validates the Order model's fields and constraints.
   *
   * @param order The model instance to validate
   * @return Array of violations found during validation
   */
  @java.lang.Override
  protected Violation[] validateModel(Order order) {
    return new ViolationAggregator()
      .add(new NumericValidator<Double>("total").min(0D).required().validate(order.getTotal()))
      .add(
        new StringValidator("currency")
          .minLength(3)
          .maxLength(3)
          .required()
          .validate(order.getCurrency())
      )
      .aggregate();
  }
}
