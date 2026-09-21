import { z } from 'zod';

export const createOrderRequestStatus = z.union([
  z.literal('pending'),
  z.literal('processing'),
  z.literal('shipped'),
  z.literal('cancelled'),
]);

export type CreateOrderRequestStatus = z.infer<typeof createOrderRequestStatus>;
