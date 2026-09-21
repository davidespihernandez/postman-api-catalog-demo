import { z } from 'zod';

export const updateOrderRequestStatus = z.union([
  z.literal('pending'),
  z.literal('processing'),
  z.literal('shipped'),
  z.literal('cancelled'),
]);

export type UpdateOrderRequestStatus = z.infer<typeof updateOrderRequestStatus>;
