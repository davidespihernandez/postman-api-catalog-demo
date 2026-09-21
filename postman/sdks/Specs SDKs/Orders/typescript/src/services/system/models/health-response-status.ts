import { z } from 'zod';

export const healthResponseStatus = z.literal('ok');

export type HealthResponseStatus = z.infer<typeof healthResponseStatus>;
