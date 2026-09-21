import { z } from 'zod';
import { ThrowableError } from '../../http/errors/throwable-error';

export type IServerErrorResponseSchema = {
  error: string;
  status?: number;
  simulated?: boolean;
  service?: string;
  delayMs?: number;
};

export const serverErrorResponseResponse = z.lazy(() => {
  return z
    .object({
      error: z.string(),
      status: z.number().optional(),
      simulated: z.boolean().optional(),
      service: z.string().optional(),
      delayMs: z.number().optional(),
    })
    .transform((data) => ({
      error: data['error'],
      status: data['status'],
      simulated: data['simulated'],
      service: data['service'],
      delayMs: data['delayMs'],
    }));
});

export class ServerErrorResponse extends ThrowableError {
  public error!: string;
  public status?: number;
  public simulated?: boolean;
  public service?: string;
  public delayMs?: number;
  constructor(
    public message: string,
    protected response?: unknown,
  ) {
    super(message);
  }

  static from(message: string, response?: unknown): ServerErrorResponse {
    const error = new ServerErrorResponse(message, response);
    const result = serverErrorResponseResponse.safeParse(response);
    const parsedResponse = (result.success ? result.data : response || {}) as z.infer<
      typeof serverErrorResponseResponse
    >;

    error.error = parsedResponse.error;
    error.status = parsedResponse.status;
    error.simulated = parsedResponse.simulated;
    error.service = parsedResponse.service;
    error.delayMs = parsedResponse.delayMs;

    return error;
  }

  public throw() {
    const error = ServerErrorResponse.from(this.message, this.response);
    error.metadata = this.metadata;
    throw error;
  }
}
