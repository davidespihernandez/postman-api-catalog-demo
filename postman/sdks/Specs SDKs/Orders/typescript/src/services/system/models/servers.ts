import { z } from 'zod';

/**
 * Zod schema for the Servers model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const servers = z.lazy(() => {
  return z.object({
    url: z.string().optional(),
    description: z.string().optional(),
  });
});

/**
 * @typedef {Servers} servers
 * @property {string} url
 * @property {string} description
 */
export type Servers = z.infer<typeof servers>;

/**
 * Zod schema for mapping API responses to the Servers application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const serversResponse = z.lazy(() => {
  return z
    .object({
      url: z.string().optional(),
      description: z.string().optional(),
    })
    .transform((data) => ({
      url: data['url'],
      description: data['description'],
    }));
});

/**
 * Zod schema for mapping the Servers application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const serversRequest = z.lazy(() => {
  return z
    .object({
      url: z.string().optional(),
      description: z.string().optional(),
    })
    .transform((data) => ({
      url: data['url'],
      description: data['description'],
    }));
});
