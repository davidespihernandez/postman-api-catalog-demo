from __future__ import annotations
from enum import Enum
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_model import BaseModel


class UpdateOrderRequestStatus(str, Enum):
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
            map(lambda x: x.value, UpdateOrderRequestStatus._member_map_.values())
        )


class UpdateOrderRequest(BaseModel):
    """UpdateOrderRequest

    :param customer_id: customer_id
    :type customer_id: str
    :param total: total
    :type total: float
    :param status: status
    :type status: UpdateOrderRequestStatus
    :param currency: currency, defaults to None
    :type currency: str, optional
    """

    customer_id: str = Field(alias="customerId", serialization_alias="customerId")
    total: float = Field(ge=0)
    status: UpdateOrderRequestStatus
    currency: Optional[str] = Field(default=None, min_length=3, max_length=3)
