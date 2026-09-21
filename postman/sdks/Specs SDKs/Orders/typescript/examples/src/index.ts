import { OrdersSdk } from 'orders-sdk';

(async () => {
  const ordersSdk = new OrdersSdk({});

  const data = await ordersSdk.system.getHealth();

  console.log(data);
})();
