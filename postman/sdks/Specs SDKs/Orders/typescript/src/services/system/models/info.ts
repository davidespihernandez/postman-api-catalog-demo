import { z } from 'zod';

/**
 * Zod schema for the Info model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const info = z.lazy(() => {
  return z.object({
    title: z.string(),
    version: z.string(),
    description: z.string().optional(),
  });
});

/**
 * @typedef {Info} info
 * @property {string} title
 * @property {string} version
 * @property {string} description
 */
export type Info = z.infer<typeof info>;

/**
 * Zod schema for mapping API responses to the Info application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const infoResponse = z.lazy(() => {
  return z
    .object({
      title: z.string(),
      version: z.string(),
      description: z.string().optional(),
    })
    .transform((data) => ({
      title: data['title'],
      version: data['version'],
      description: data['description'],
    }));
});

/**
 * Zod schema for mapping the Info application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const infoRequest = z.lazy(() => {
  return z
    .object({
      title: z.string(),
      version: z.string(),
      description: z.string().optional(),
    })
    .transform((data) => ({
      title: data['title'],
      version: data['version'],
      description: data['description'],
    }));
});
