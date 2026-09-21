from typing import Union
from .net.environment import Environment
from .sdk import OrdersSdk
from .services.async_.system import SystemServiceAsync
from .services.async_.orders import OrdersServiceAsync


class OrdersSdkAsync(OrdersSdk):
    """
    OrdersSdkAsync is the asynchronous version of the OrdersSdk SDK Client.
    """

    def __init__(
        self,
        *,
        base_url: Union[Environment, str, None] = None,
        timeout: float = None,
        timeout_ms: int = None,
        retry: "RetryConfig" = None,
    ):
        super().__init__(
            base_url=base_url, timeout=timeout, timeout_ms=timeout_ms, retry=retry
        )

        self.system = SystemServiceAsync(base_url=self._base_url)
        self.orders = OrdersServiceAsync(base_url=self._base_url)
        if retry is not None:
            self.set_retry(retry)
