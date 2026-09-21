import { z } from 'zod';
import { Info, info, infoRequest, infoResponse } from './info';
import { Servers, servers, serversRequest, serversResponse } from './servers';

/**
 * Zod schema for the OpenApiDocument model.
 * Defines the structure and validation rules for this data type.
 * This is the shape used in application code - what developers interact with.
 */
export const openApiDocument = z.lazy(() => {
  return z.object({
    openapi: z.string(),
    info: info,
    servers: z.array(servers).optional(),
    paths: z.any(),
    components: z.any().optional(),
  });
});

/**
 * OpenAPI 3.0 document
 * @typedef {OpenApiDocument} openApiDocument
 * @property {string} openapi
 * @property {Info} info
 * @property {Servers[]} servers
 * @property {any} paths
 * @property {any} components
 */
export type OpenApiDocument = z.infer<typeof openApiDocument>;

/**
 * Zod schema for mapping API responses to the OpenApiDocument application shape.
 * Handles any property name transformations from the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const openApiDocumentResponse = z.lazy(() => {
  return z
    .object({
      openapi: z.string(),
      info: infoResponse,
      servers: z.array(serversResponse).optional(),
      paths: z.any(),
      components: z.any().optional(),
    })
    .transform((data) => ({
      openapi: data['openapi'],
      info: data['info'],
      servers: data['servers'],
      paths: data['paths'],
      components: data['components'],
    }));
});

/**
 * Zod schema for mapping the OpenApiDocument application shape to API requests.
 * Handles any property name transformations required by the API schema.
 * If property names match the API schema exactly, this is identical to the application shape.
 */
export const openApiDocumentRequest = z.lazy(() => {
  return z
    .object({
      openapi: z.string(),
      info: infoRequest,
      servers: z.array(serversRequest).optional(),
      paths: z.any(),
      components: z.any().optional(),
    })
    .transform((data) => ({
      openapi: data['openapi'],
      info: data['info'],
      servers: data['servers'],
      paths: data['paths'],
      components: data['components'],
    }));
});
