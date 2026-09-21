package com.orderssdk.validation.validators.modelValidators;

import com.orderssdk.models.PatchOrderRequest;
import com.orderssdk.validation.Violation;
import com.orderssdk.validation.ViolationAggregator;
import com.orderssdk.validation.validators.NumericValidator;
import com.orderssdk.validation.validators.StringValidator;

/**
 * Validator implementation for PatchOrderRequest model.
 * Validates all fields and nested structures according to the model's constraints.
 */
public class PatchOrderRequestValidator extends AbstractModelValidator<PatchOrderRequest> {

  /**
   * Creates a validator with a field name for nested validation paths.
   *
   * @param fieldName The field name to use in violation paths
   */
  public PatchOrderRequestValidator(String fieldName) {
    super(fieldName);
  }

  /**
   * Creates a validator for root-level validation.
   */
  public PatchOrderRequestValidator() {}

  /**
   * Validates the PatchOrderRequest model's fields and constraints.
   *
   * @param patchOrderRequest The model instance to validate
   * @return Array of violations found during validation
   */
  @java.lang.Override
  protected Violation[] validateModel(PatchOrderRequest patchOrderRequest) {
    return new ViolationAggregator()
      .add(
        new NumericValidator<Double>("total")
          .min(0D)
          .optional()
          .validate(patchOrderRequest.getTotal())
      )
      .add(
        new StringValidator("currency")
          .minLength(3)
          .maxLength(3)
          .optional()
          .validate(patchOrderRequest.getCurrency())
      )
      .aggregate();
  }
}
