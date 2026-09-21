from typing import Any, Optional, Union
from .utils.validator import Validator
from .utils.base_service import BaseService
from ..net.transport.serializer import Serializer
from ..net.sdk_config import should_validate_response
from ..net.sdk_config import SdkConfig
from ..net.environment.environment import Environment
from ..models.utils.cast_models import cast_models
from ..models import HealthResponse, OpenApiDocument, ServerErrorResponse


class SystemService(BaseService):
    """
    Service class for SystemService operations.
    Provides methods to interact with SystemService-related API endpoints.
    Inherits common functionality from BaseService including authentication and request handling.
    """

    def __init__(self, *args, **kwargs):
        """Initialize the service and method-level configurations."""
        super().__init__(*args, **kwargs)
        self._get_health_config: SdkConfig = {}
        self._get_open_api_config: SdkConfig = {}

    def set_get_health_config(self, config: SdkConfig):
        """
        Sets method-level configuration for get_health.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._get_health_config = config
        return self

    def set_get_open_api_config(self, config: SdkConfig):
        """
        Sets method-level configuration for get_open_api.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._get_open_api_config = config
        return self

    @cast_models
    def get_health(
        self, *, request_config: Optional[SdkConfig] = None
    ) -> HealthResponse:
        """get_health

        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: HealthResponse
        """

        resolved_config = self._get_resolved_config(
            self._get_health_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/health",
                [],
                resolved_config,
            )
            .add_error(500, ServerErrorResponse)
            .serialize()
            .set_method("GET")
        )

        response, status, _ = self.send_request(serialized_request)
        return (
            None
            if response in (b"", "")
            else (
                HealthResponse.model_validate(response)
                if should_validate_response(resolved_config)
                else response
            )
        )

    @cast_models
    def get_open_api(
        self, *, request_config: Optional[SdkConfig] = None
    ) -> OpenApiDocument:
        """get_open_api

        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: OpenApiDocument
        """

        resolved_config = self._get_resolved_config(
            self._get_open_api_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/openapi.json",
                [],
                resolved_config,
            )
            .add_error(500, ServerErrorResponse)
            .serialize()
            .set_method("GET")
        )

        response, status, _ = self.send_request(serialized_request)
        return (
            None
            if response in (b"", "")
            else (
                OpenApiDocument.model_validate(response)
                if should_validate_response(resolved_config)
                else response
            )
        )
