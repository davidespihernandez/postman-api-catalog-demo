import { z } from 'zod';

export const patchOrderRequestStatus = z.union([
  z.literal('pending'),
  z.literal('processing'),
  z.literal('shipped'),
  z.literal('cancelled'),
]);

export type PatchOrderRequestStatus = z.infer<typeof patchOrderRequestStatus>;
