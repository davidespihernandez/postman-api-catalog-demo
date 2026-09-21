import { z } from 'zod';
import { PatchOrderRequestStatus, patchOrderRequestStatus } from './patch-order-request-status';

/**
 * Zod schema for the PatchOrderRequest model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const patchOrderRequest = z.lazy(() => {
  return z.object({
    customerId: z.string().optional(),
    total: z.number().gte(0).optional(),
    status: patchOrderRequestStatus.optional(),
    currency: z.string().min(3).max(3).optional(),
  });
});

/**
 * @typedef {PatchOrderRequest} patchOrderRequest
 * @property {string} customerId
 * @property {number} total
 * @property {PatchOrderRequestStatus} status
 * @property {string} currency
 */
export type PatchOrderRequest = z.infer<typeof patchOrderRequest>;

/**
 * Zod schema for mapping API responses to the PatchOrderRequest application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const patchOrderRequestResponse = z.lazy(() => {
  return z
    .object({
      customerId: z.string().optional(),
      total: z.number().gte(0).optional(),
      status: patchOrderRequestStatus.optional(),
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
 * Zod schema for mapping the PatchOrderRequest application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const patchOrderRequestRequest = z.lazy(() => {
  return z
    .object({
      customerId: z.string().optional(),
      total: z.number().gte(0).optional(),
      status: patchOrderRequestStatus.optional(),
      currency: z.string().min(3).max(3).optional(),
    })
    .transform((data) => ({
      customerId: data['customerId'],
      total: data['total'],
      status: data['status'],
      currency: data['currency'],
    }));
});
