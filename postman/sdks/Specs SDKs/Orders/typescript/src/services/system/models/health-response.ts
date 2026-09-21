import { z } from 'zod';
import { HealthResponseStatus, healthResponseStatus } from './health-response-status';

/**
 * Zod schema for the HealthResponse model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const healthResponse = z.lazy(() => {
  return z.object({
    status: healthResponseStatus,
    service: z.string(),
    version: z.string(),
  });
});

/**
 * @typedef {HealthResponse} healthResponse
 * @property {HealthResponseStatus} status
 * @property {string} service
 * @property {string} version
 */
export type HealthResponse = z.infer<typeof healthResponse>;

/**
 * Zod schema for mapping API responses to the HealthResponse application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const healthResponseResponse = z.lazy(() => {
  return z
    .object({
      status: healthResponseStatus,
      service: z.string(),
      version: z.string(),
    })
    .transform((data) => ({
      status: data['status'],
      service: data['service'],
      version: data['version'],
    }));
});

/**
 * Zod schema for mapping the HealthResponse application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const healthResponseRequest = z.lazy(() => {
  return z
    .object({
      status: healthResponseStatus,
      service: z.string(),
      version: z.string(),
    })
    .transform((data) => ({
      status: data['status'],
      service: data['service'],
      version: data['version'],
    }));
});
