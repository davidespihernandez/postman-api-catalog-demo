import { z } from 'zod';
import { OrderStatus, orderStatus } from './order-status';

/**
 * Zod schema for the Order model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const order = z.lazy(() => {
  return z.object({
    id: z.string(),
    customerId: z.string(),
    status: orderStatus,
    total: z.number().gte(0),
    currency: z.string().min(3).max(3),
  });
});

/**
 * @typedef {Order} order
 * @property {string} id - Unique order identifier
 * @property {string} customerId - ID of the customer who placed the order
 * @property {OrderStatus} status - Order lifecycle status
 * @property {number} total - Order total amount
 * @property {string} currency - ISO 4217 currency code
 */
export type Order = z.infer<typeof order>;

/**
 * Zod schema for mapping API responses to the Order application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const orderResponse = z.lazy(() => {
  return z
    .object({
      id: z.string(),
      customerId: z.string(),
      status: orderStatus,
      total: z.number().gte(0),
      currency: z.string().min(3).max(3),
    })
    .transform((data) => ({
      id: data['id'],
      customerId: data['customerId'],
      status: data['status'],
      total: data['total'],
      currency: data['currency'],
    }));
});

/**
 * Zod schema for mapping the Order application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const orderRequest = z.lazy(() => {
  return z
    .object({
      id: z.string(),
      customerId: z.string(),
      status: orderStatus,
      total: z.number().gte(0),
      currency: z.string().min(3).max(3),
    })
    .transform((data) => ({
      id: data['id'],
      customerId: data['customerId'],
      status: data['status'],
      total: data['total'],
      currency: data['currency'],
    }));
});
