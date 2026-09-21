import { z } from 'zod';
import { UpdateOrderRequestStatus, updateOrderRequestStatus } from './update-order-request-status';

/**
 * Zod schema for the UpdateOrderRequest model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const updateOrderRequest = z.lazy(() => {
  return z.object({
    customerId: z.string(),
    total: z.number().gte(0),
    status: updateOrderRequestStatus,
    currency: z.string().min(3).max(3).optional(),
  });
});

/**
 * @typedef {UpdateOrderRequest} updateOrderRequest
 * @property {string} customerId
 * @property {number} total
 * @property {UpdateOrderRequestStatus} status
 * @property {string} currency
 */
export type UpdateOrderRequest = z.infer<typeof updateOrderRequest>;

/**
 * Zod schema for mapping API responses to the UpdateOrderRequest application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const updateOrderRequestResponse = z.lazy(() => {
  return z
    .object({
      customerId: z.string(),
      total: z.number().gte(0),
      status: updateOrderRequestStatus,
      currency: z.string().min(3).max(3).optional(),
    })
    .transform((data) => ({
      customerId: data['customerId'],
      total: data['total'],
      status: data['status'],
      currency: data['currency'],
    }));
});

/**
 * Zod schema for mapping the UpdateOrderRequest application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const updateOrderRequestRequest = z.lazy(() => {
  return z
    .object({
      customerId: z.string(),
      total: z.number().gte(0),
      status: updateOrderRequestStatus,
      currency: z.string().min(3).max(3).optional(),
    })
    .transform((data) => ({
      customerId: data['customerId'],
      total: data['total'],
      status: data['status'],
      currency: data['currency'],
    }));
});
