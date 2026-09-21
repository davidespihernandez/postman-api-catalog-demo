from __future__ import annotations
from typing import List
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_model import BaseModel
from .order import Order


class OrderListResponse(BaseModel):
    """OrderListResponse

    :param data: Orders in this page
    :type data: List[Order]
    :param count: Number of orders returned
    :type count: int
    """

    data: List[Order] = Field(description="Orders in this page")
    count: int = Field(description="Number of orders returned", ge=0)
