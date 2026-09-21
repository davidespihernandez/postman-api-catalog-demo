import { Environment } from './http/environment';
import { SdkConfig } from './http/types';
import { SystemService } from './services/system';
import { OrdersService } from './services/orders';

export * from './services/system';
export * from './services/orders';
export * from './services/common';

export * from './http';
export { Environment } from './http/environment';

export class OrdersSdk {
  public readonly system: SystemService;

  public readonly orders: OrdersService;

  constructor(public config: SdkConfig) {
    this.system = new SystemService(this.config);

    this.orders = new OrdersService(this.config);
  }

  set baseUrl(baseUrl: string) {
    this.system.baseUrl = baseUrl;
    this.orders.baseUrl = baseUrl;
  }

  set environment(environment: Environment) {
    this.system.baseUrl = environment;
    this.orders.baseUrl = environment;
  }

  set timeoutMs(timeoutMs: number) {
    this.system.timeoutMs = timeoutMs;
    this.orders.timeoutMs = timeoutMs;
  }
}

// c029837e0e474b76bc487506e8799df5e3335891efe4fb02bda7a1441840310c
