from orders_sdk import OrdersSdk

sdk = OrdersSdk(timeout=10)

result = sdk.system.get_health()

print(result)
