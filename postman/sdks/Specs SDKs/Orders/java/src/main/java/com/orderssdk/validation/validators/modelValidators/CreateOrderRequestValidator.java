package com.orderssdk.validation.validators.modelValidators;

import com.orderssdk.models.CreateOrderRequest;
import com.orderssdk.validation.Violation;
import com.orderssdk.validation.ViolationAggregator;
import com.orderssdk.validation.validators.NumericValidator;
import com.orderssdk.validation.validators.StringValidator;

/**
 * Validator implementation for CreateOrderRequest model.
 * Validates all fields and nested structures according to the model's constraints.
 */
public class CreateOrderRequestValidator extends AbstractModelValidator<CreateOrderRequest> {

  /**
   * Creates a validator with a field name for nested validation paths.
   *
   * @param fieldName The field name to use in violation paths
   */
  public CreateOrderRequestValidator(String fieldName) {
    super(fieldName);
  }

  /**
   * Creates a validator for root-level validation.
   */
  public CreateOrderRequestValidator() {}

  /**
   * Validates the CreateOrderRequest model's fields and constraints.
   *
   * @param createOrderRequest The model instance to validate
   * @return Array of violations found during validation
   */
  @java.lang.Override
  protected Violation[] validateModel(CreateOrderRequest createOrderRequest) {
    return new ViolationAggregator()
      .add(
        new NumericValidator<Double>("total")
          .min(0D)
          .required()
          .validate(createOrderRequest.getTotal())
      )
      .add(
        new StringValidator("currency")
          .minLength(3)
          .maxLength(3)
          .optional()
          .validate(createOrderRequest.getCurrency())
      )
      .aggregate();
  }
}
