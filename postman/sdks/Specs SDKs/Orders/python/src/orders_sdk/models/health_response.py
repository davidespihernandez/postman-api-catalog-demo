from __future__ import annotations
from enum import Enum
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_model import BaseModel


class HealthResponseStatus(str, Enum):
    """An enumeration representing different categories.

    :cvar OK: "ok"
    :vartype OK: str
    """

    OK = "ok"

    @staticmethod
    def list():
        """Lists all enum values.

        :return: A list of all enum values.
        :rtype: list
        """
        return list(map(lambda x: x.value, HealthResponseStatus._member_map_.values()))


class HealthResponse(BaseModel):
    """HealthResponse

    :param status: status
    :type status: HealthResponseStatus
    :param service: service
    :type service: str
    :param version: version
    :type version: str
    """

    status: HealthResponseStatus
    service: str
    version: str
