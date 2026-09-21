import { z } from 'zod';
import { Order, order, orderRequest, orderResponse } from './order';

/**
 * Zod schema for the OrderListResponse model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const orderListResponse = z.lazy(() => {
  return z.object({
    data: z.array(order),
    count: z.number().gte(0),
  });
});

/**
 * @typedef {OrderListResponse} orderListResponse
 * @property {Order[]} data - Orders in this page
 * @property {number} count - Number of orders returned
 */
export type OrderListResponse = z.infer<typeof orderListResponse>;

/**
 * Zod schema for mapping API responses to the OrderListResponse application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const orderListResponseResponse = z.lazy(() => {
  return z
    .object({
      data: z.array(orderResponse),
      count: z.number().gte(0),
    })
    .transform((data) => ({
      data: data['data'],
      count: data['count'],
    }));
});

/**
 * Zod schema for mapping the OrderListResponse application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const orderListResponseRequest = z.lazy(() => {
  return z
    .object({
      data: z.array(orderRequest),
      count: z.number().gte(0),
    })
    .transform((data) => ({
      data: data['data'],
      count: data['count'],
    }));
});
