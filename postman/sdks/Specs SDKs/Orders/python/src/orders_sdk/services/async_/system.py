from typing import Awaitable, Optional, Any, Union
from .utils.to_async import to_async
from ..system import SystemService
from ...net.sdk_config import SdkConfig
from ...models import HealthResponse, OpenApiDocument


class SystemServiceAsync(SystemService):
    """
    Async Wrapper for SystemServiceAsync
    """

    def get_health(
        self, *, request_config: Optional[SdkConfig] = None
    ) -> Awaitable[HealthResponse]:
        return to_async(super().get_health)(request_config=request_config)

    def get_open_api(
        self, *, request_config: Optional[SdkConfig] = None
    ) -> Awaitable[OpenApiDocument]:
        return to_async(super().get_open_api)(request_config=request_config)
