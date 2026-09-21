from __future__ import annotations
from enum import Enum
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_model import BaseModel


class PatchOrderRequestStatus(str, Enum):
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
            map(lambda x: x.value, PatchOrderRequestStatus._member_map_.values())
        )


class PatchOrderRequest(BaseModel):
    """PatchOrderRequest

    :param customer_id: customer_id, defaults to None
    :type customer_id: str, optional
    :param total: total, defaults to None
    :type total: float, optional
    :param status: status, defaults to None
    :type status: PatchOrderRequestStatus, optional
    :param currency: currency, defaults to None
    :type currency: str, optional
    """

    customer_id: Optional[str] = Field(
        alias="customerId", serialization_alias="customerId", default=None
    )
    total: Optional[float] = Field(default=None, ge=0)
    status: Optional[PatchOrderRequestStatus] = Field(default=None)
    currency: Optional[str] = Field(default=None, min_length=3, max_length=3)
