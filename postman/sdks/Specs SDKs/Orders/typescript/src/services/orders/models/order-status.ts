import { z } from 'zod';

export const orderStatus = z.union([
  z.literal('pending'),
  z.literal('processing'),
  z.literal('shipped'),
  z.literal('cancelled'),
]);

export type OrderStatus = z.infer<typeof orderStatus>;
