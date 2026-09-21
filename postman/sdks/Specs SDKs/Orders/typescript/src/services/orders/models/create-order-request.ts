import { z } from 'zod';
import { CreateOrderRequestStatus, createOrderRequestStatus } from './create-order-request-status';

/**
 * Zod schema for the CreateOrderRequest model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const createOrderRequest = z.lazy(() => {
  return z.object({
    customerId: z.string(),
    total: z.number().gte(0),
    status: createOrderRequestStatus.optional(),
    currency: z.string().min(3).max(3).optional(),
  });
});

/**
 * @typedef {CreateOrderRequest} createOrderRequest
 * @property {string} customerId - Customer placing the order
 * @property {number} total
 * @property {CreateOrderRequestStatus} status - Initial order status
 * @property {string} currency - ISO 4217 currency code
 */
export type CreateOrderRequest = z.infer<typeof createOrderRequest>;

/**
 * Zod schema for mapping API responses to the CreateOrderRequest application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const createOrderRequestResponse = z.lazy(() => {
  return z
    .object({
      customerId: z.string(),
      total: z.number().gte(0),
      status: createOrderRequestStatus.optional(),
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
 * Zod schema for mapping the CreateOrderRequest application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const createOrderRequestRequest = z.lazy(() => {
  return z
    .object({
      customerId: z.string(),
      total: z.number().gte(0),
      status: createOrderRequestStatus.optional(),
      currency: z.string().min(3).max(3).optional(),
    })
    .transform((data) => ({
      customerId: data['customerId'],
      total: data['total'],
      status: data['status'],
      currency: data['currency'],
    }));
});
