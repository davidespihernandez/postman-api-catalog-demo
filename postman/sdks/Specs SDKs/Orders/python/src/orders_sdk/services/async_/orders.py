from typing import Awaitable, Optional, Any, Union
from .utils.to_async import to_async
from ..orders import OrdersService
from ...net.sdk_config import SdkConfig
from ...models import (
    OrderListResponse,
    Order,
    CreateOrderRequest,
    UpdateOrderRequest,
    PatchOrderRequest,
)


class OrdersServiceAsync(OrdersService):
    """
    Async Wrapper for OrdersServiceAsync
    """

    def list_orders(
        self, *, request_config: Optional[SdkConfig] = None
    ) -> Awaitable[OrderListResponse]:
        return to_async(super().list_orders)(request_config=request_config)

    def create_order(
        self,
        request_body: CreateOrderRequest,
        *,
        request_config: Optional[SdkConfig] = None,
    ) -> Awaitable[Order]:
        return to_async(super().create_order)(
            request_body, request_config=request_config
        )

    def get_order(
        self, id_: str, *, request_config: Optional[SdkConfig] = None
    ) -> Awaitable[Order]:
        return to_async(super().get_order)(id_, request_config=request_config)

    def update_order(
        self,
        request_body: UpdateOrderRequest,
        id_: str,
        *,
        request_config: Optional[SdkConfig] = None,
    ) -> Awaitable[Order]:
        return to_async(super().update_order)(
            request_body, id_, request_config=request_config
        )

    def patch_order(
        self,
        request_body: PatchOrderRequest,
        id_: str,
        *,
        request_config: Optional[SdkConfig] = None,
    ) -> Awaitable[Order]:
        return to_async(super().patch_order)(
            request_body, id_, request_config=request_config
        )

    def delete_order(
        self, id_: str, *, request_config: Optional[SdkConfig] = None
    ) -> Awaitable[None]:
        return to_async(super().delete_order)(id_, request_config=request_config)
