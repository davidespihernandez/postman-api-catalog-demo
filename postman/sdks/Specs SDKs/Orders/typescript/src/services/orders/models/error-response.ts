import { z } from 'zod';
import { ThrowableError } from '../../../http/errors/throwable-error';

export type IErrorResponseSchema = {
  error: string;
  service?: string;
};

export const errorResponseResponse = z.lazy(() => {
  return z
    .object({
      error: z.string(),
      service: z.string().optional(),
    })
    .transform((data) => ({
      error: data['error'],
      service: data['service'],
    }));
});

export class ErrorResponse extends ThrowableError {
  public error!: string;
  public service?: string;
  constructor(
    public message: string,
    protected response?: unknown,
  ) {
    super(message);
  }

  static from(message: string, response?: unknown): ErrorResponse {
    const error = new ErrorResponse(message, response);
    const result = errorResponseResponse.safeParse(response);
    const parsedResponse = (result.success ? result.data : response || {}) as z.infer<
      typeof errorResponseResponse
    >;

    error.error = parsedResponse.error;
    error.service = parsedResponse.service;

    return error;
  }

  public throw() {
    const error = ErrorResponse.from(this.message, this.response);
    error.metadata = this.metadata;
    throw error;
  }
}
