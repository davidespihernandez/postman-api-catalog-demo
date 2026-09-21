package com.orderssdk.validation.validators.modelValidators;

import com.orderssdk.models.UpdateOrderRequest;
import com.orderssdk.validation.Violation;
import com.orderssdk.validation.ViolationAggregator;
import com.orderssdk.validation.validators.NumericValidator;
import com.orderssdk.validation.validators.StringValidator;

/**
 * Validator implementation for UpdateOrderRequest model.
 * Validates all fields and nested structures according to the model's constraints.
 */
public class UpdateOrderRequestValidator extends AbstractModelValidator<UpdateOrderRequest> {

  /**
   * Creates a validator with a field name for nested validation paths.
   *
   * @param fieldName The field name to use in violation paths
   */
  public UpdateOrderRequestValidator(String fieldName) {
    super(fieldName);
  }

  /**
   * Creates a validator for root-level validation.
   */
  public UpdateOrderRequestValidator() {}

  /**
   * Validates the UpdateOrderRequest model's fields and constraints.
   *
   * @param updateOrderRequest The model instance to validate
   * @return Array of violations found during validation
   */
  @java.lang.Override
  protected Violation[] validateModel(UpdateOrderRequest updateOrderRequest) {
    return new ViolationAggregator()
      .add(
        new NumericValidator<Double>("total")
          .min(0D)
          .required()
          .validate(updateOrderRequest.getTotal())
      )
      .add(
        new StringValidator("currency")
          .minLength(3)
          .maxLength(3)
          .optional()
          .validate(updateOrderRequest.getCurrency())
      )
      .aggregate();
  }
}
