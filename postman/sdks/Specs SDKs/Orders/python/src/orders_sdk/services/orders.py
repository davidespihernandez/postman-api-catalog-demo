from typing import Any, Optional, Union
from .utils.validator import Validator
from .utils.base_service import BaseService
from ..net.transport.serializer import Serializer
from ..net.sdk_config import should_validate_response
from ..net.sdk_config import SdkConfig
from ..net.environment.environment import Environment
from ..models.utils.cast_models import cast_models
from ..models import (
    CreateOrderRequest,
    ErrorResponse,
    Order,
    OrderListResponse,
    PatchOrderRequest,
    ServerErrorResponse,
    UpdateOrderRequest,
)


class OrdersService(BaseService):
    """
    Service class for OrdersService operations.
    Provides methods to interact with OrdersService-related API endpoints.
    Inherits common functionality from BaseService including authentication and request handling.
    """

    def __init__(self, *args, **kwargs):
        """Initialize the service and method-level configurations."""
        super().__init__(*args, **kwargs)
        self._list_orders_config: SdkConfig = {}
        self._create_order_config: SdkConfig = {}
        self._get_order_config: SdkConfig = {}
        self._update_order_config: SdkConfig = {}
        self._patch_order_config: SdkConfig = {}
        self._delete_order_config: SdkConfig = {}

    def set_list_orders_config(self, config: SdkConfig):
        """
        Sets method-level configuration for list_orders.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._list_orders_config = config
        return self

    def set_create_order_config(self, config: SdkConfig):
        """
        Sets method-level configuration for create_order.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._create_order_config = config
        return self

    def set_get_order_config(self, config: SdkConfig):
        """
        Sets method-level configuration for get_order.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._get_order_config = config
        return self

    def set_update_order_config(self, config: SdkConfig):
        """
        Sets method-level configuration for update_order.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._update_order_config = config
        return self

    def set_patch_order_config(self, config: SdkConfig):
        """
        Sets method-level configuration for patch_order.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._patch_order_config = config
        return self

    def set_delete_order_config(self, config: SdkConfig):
        """
        Sets method-level configuration for delete_order.

        :param SdkConfig config: Configuration dictionary to override service-level defaults.
        :return: The service instance for method chaining.
        """
        self._delete_order_config = config
        return self

    @cast_models
    def list_orders(
        self, *, request_config: Optional[SdkConfig] = None
    ) -> OrderListResponse:
        """list_orders

        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: OrderListResponse
        """

        resolved_config = self._get_resolved_config(
            self._list_orders_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/orders",
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
                OrderListResponse.model_validate(response)
                if should_validate_response(resolved_config)
                else response
            )
        )

    @cast_models
    def create_order(
        self,
        request_body: CreateOrderRequest,
        *,
        request_config: Optional[SdkConfig] = None,
    ) -> Order:
        """create_order

        :param request_body: The request body.
        :type request_body: CreateOrderRequest
        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: Order
        """

        Validator(CreateOrderRequest).validate(request_body, "request_body")

        resolved_config = self._get_resolved_config(
            self._create_order_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/orders",
                [],
                resolved_config,
            )
            .add_error(400, ErrorResponse)
            .add_error(500, ServerErrorResponse)
            .serialize()
            .set_method("POST")
            .set_body(request_body)
        )

        response, status, _ = self.send_request(serialized_request)
        return (
            None
            if response in (b"", "")
            else (
                Order.model_validate(response)
                if should_validate_response(resolved_config)
                else response
            )
        )

    @cast_models
    def get_order(
        self, id_: str, *, request_config: Optional[SdkConfig] = None
    ) -> Order:
        """get_order

        :param id_: Order identifier
        :type id_: str
        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: Order
        """

        Validator(str).validate(id_, "id_")

        resolved_config = self._get_resolved_config(
            self._get_order_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/orders/{{id}}",
                [],
                resolved_config,
            )
            .add_path("id", id_)
            .add_error(404, ErrorResponse)
            .add_error(500, ServerErrorResponse)
            .serialize()
            .set_method("GET")
        )

        response, status, _ = self.send_request(serialized_request)
        return (
            None
            if response in (b"", "")
            else (
                Order.model_validate(response)
                if should_validate_response(resolved_config)
                else response
            )
        )

    @cast_models
    def update_order(
        self,
        request_body: UpdateOrderRequest,
        id_: str,
        *,
        request_config: Optional[SdkConfig] = None,
    ) -> Order:
        """update_order

        :param request_body: The request body.
        :type request_body: UpdateOrderRequest
        :param id_: Order identifier
        :type id_: str
        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: Order
        """

        Validator(UpdateOrderRequest).validate(request_body, "request_body")
        Validator(str).validate(id_, "id_")

        resolved_config = self._get_resolved_config(
            self._update_order_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/orders/{{id}}",
                [],
                resolved_config,
            )
            .add_path("id", id_)
            .add_error(400, ErrorResponse)
            .add_error(404, ErrorResponse)
            .add_error(500, ServerErrorResponse)
            .serialize()
            .set_method("PUT")
            .set_body(request_body)
        )

        response, status, _ = self.send_request(serialized_request)
        return (
            None
            if response in (b"", "")
            else (
                Order.model_validate(response)
                if should_validate_response(resolved_config)
                else response
            )
        )

    @cast_models
    def patch_order(
        self,
        request_body: PatchOrderRequest,
        id_: str,
        *,
        request_config: Optional[SdkConfig] = None,
    ) -> Order:
        """patch_order

        :param request_body: The request body.
        :type request_body: PatchOrderRequest
        :param id_: Order identifier
        :type id_: str
        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: Order
        """

        Validator(PatchOrderRequest).validate(request_body, "request_body")
        Validator(str).validate(id_, "id_")

        resolved_config = self._get_resolved_config(
            self._patch_order_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/orders/{{id}}",
                [],
                resolved_config,
            )
            .add_path("id", id_)
            .add_error(404, ErrorResponse)
            .add_error(500, ServerErrorResponse)
            .serialize()
            .set_method("PATCH")
            .set_body(request_body)
        )

        response, status, _ = self.send_request(serialized_request)
        return (
            None
            if response in (b"", "")
            else (
                Order.model_validate(response)
                if should_validate_response(resolved_config)
                else response
            )
        )

    @cast_models
    def delete_order(
        self, id_: str, *, request_config: Optional[SdkConfig] = None
    ) -> None:
        """delete_order

        :param id_: Order identifier
        :type id_: str
        ...
        :raises RequestError: Raised when a request fails, with optional HTTP status code and details.
        ...
        :return: The parsed response data.
        :rtype: None
        """

        Validator(str).validate(id_, "id_")

        resolved_config = self._get_resolved_config(
            self._delete_order_config, request_config
        )

        serialized_request = (
            Serializer(
                f"{self._resolve_base_url(resolved_config) or Environment.DEFAULT.url}/orders/{{id}}",
                [],
                resolved_config,
            )
            .add_path("id", id_)
            .add_error(404, ErrorResponse)
            .add_error(500, ServerErrorResponse)
            .serialize()
            .set_method("DELETE")
        )

        response, status, content = self.send_request(serialized_request)
