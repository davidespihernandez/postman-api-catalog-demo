package com.orderssdk.validation.validators.modelValidators;

import com.orderssdk.models.Order;
import com.orderssdk.models.OrderListResponse;
import com.orderssdk.validation.Violation;
import com.orderssdk.validation.ViolationAggregator;
import com.orderssdk.validation.validators.ListValidator;
import com.orderssdk.validation.validators.NumericValidator;

/**
 * Validator implementation for OrderListResponse model.
 * Validates all fields and nested structures according to the model's constraints.
 */
public class OrderListResponseValidator extends AbstractModelValidator<OrderListResponse> {

  /**
   * Creates a validator with a field name for nested validation paths.
   *
   * @param fieldName The field name to use in violation paths
   */
  public OrderListResponseValidator(String fieldName) {
    super(fieldName);
  }

  /**
   * Creates a validator for root-level validation.
   */
  public OrderListResponseValidator() {}

  /**
   * Validates the OrderListResponse model's fields and constraints.
   *
   * @param orderListResponse The model instance to validate
   * @return Array of violations found during validation
   */
  @java.lang.Override
  protected Violation[] validateModel(OrderListResponse orderListResponse) {
    return new ViolationAggregator()
      .add(
        new ListValidator<Order>("data")
          .itemValidator(new OrderValidator().required())
          .required()
          .validate(orderListResponse.getData())
      )
      .add(
        new NumericValidator<Long>("count")
          .min(0L)
          .required()
          .validate(orderListResponse.getCount())
      )
      .aggregate();
  }
}
