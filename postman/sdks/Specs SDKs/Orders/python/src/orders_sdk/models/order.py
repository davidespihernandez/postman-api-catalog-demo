from __future__ import annotations
from enum import Enum
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_model import BaseModel


class OrderStatus(str, Enum):
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
        return list(map(lambda x: x.value, OrderStatus._member_map_.values()))


class Order(BaseModel):
    """Order

    :param id_: Unique order identifier
    :type id_: str
    :param customer_id: ID of the customer who placed the order
    :type customer_id: str
    :param status: Order lifecycle status
    :type status: OrderStatus
    :param total: Order total amount
    :type total: float
    :param currency: ISO 4217 currency code
    :type currency: str
    """

    id_: str = Field(
        alias="id", serialization_alias="id", description="Unique order identifier"
    )
    customer_id: str = Field(
        alias="customerId",
        serialization_alias="customerId",
        description="ID of the customer who placed the order",
    )
    status: OrderStatus = Field(description="Order lifecycle status")
    total: float = Field(description="Order total amount", ge=0)
    currency: str = Field(
        description="ISO 4217 currency code", min_length=3, max_length=3
    )
