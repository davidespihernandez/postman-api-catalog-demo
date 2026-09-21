from __future__ import annotations
from enum import Enum
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_model import BaseModel


class CreateOrderRequestStatus(str, Enum):
    """An enumeration representing different categories.

    :cvar PENDING: "pending"
    :vartype PENDING: str
    :cvar PROCESSING: "processing"
    :vartype PROCESSING: str
    :cvar SHIPPED: "shipped"
    :vartype SHIPPED: str
    :cvar CANCELLED: "cancelled"
    :vartype CANCELLED: str
    """

    PENDING = "pending"
    PROCESSING = "processing"
    SHIPPED = "shipped"
    CANCELLED = "cancelled"

    @staticmethod
    def list():
        """Lists all enum values.

        :return: A list of all enum values.
        :rtype: list
        """
        return list(
            map(lambda x: x.value, CreateOrderRequestStatus._member_map_.values())
        )


class CreateOrderRequest(BaseModel):
    """CreateOrderRequest

    :param customer_id: Customer placing the order
    :type customer_id: str
    :param total: total
    :type total: float
    :param status: Initial order status, defaults to None
    :type status: CreateOrderRequestStatus, optional
    :param currency: ISO 4217 currency code, defaults to None
    :type currency: str, optional
    """

    customer_id: str = Field(
        alias="customerId",
        serialization_alias="customerId",
        description="Customer placing the order",
    )
    total: float = Field(ge=0)
    status: Optional[CreateOrderRequestStatus] = Field(
        default=None, description="Initial order status"
    )
    currency: Optional[str] = Field(
        default=None, description="ISO 4217 currency code", min_length=3, max_length=3
    )
