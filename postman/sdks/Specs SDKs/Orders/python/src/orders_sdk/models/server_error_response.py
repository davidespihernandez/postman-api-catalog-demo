from __future__ import annotations
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_error import BaseError
from .utils.base_model import BaseModel


# Pydantic validation model for ServerErrorResponse
class ServerErrorResponseData(BaseModel):
    """ServerErrorResponse

    :param error: error
    :type error: str
    :param status: status, defaults to None
    :type status: int, optional
    :param simulated: simulated, defaults to None
    :type simulated: bool, optional
    :param service: service, defaults to None
    :type service: str, optional
    :param delay_ms: delay_ms, defaults to None
    :type delay_ms: int, optional
    """

    error: str
    status: Optional[int] = Field(default=None)
    simulated: Optional[bool] = Field(default=None)
    service: Optional[str] = Field(default=None)
    delay_ms: Optional[int] = Field(
        alias="delayMs", serialization_alias="delayMs", default=None
    )


# Error exception class
class ServerErrorResponse(BaseError):
    """ServerErrorResponse

    :param error: error
    :type error: str
    :param status: status, defaults to None
    :type status: int, optional
    :param simulated: simulated, defaults to None
    :type simulated: bool, optional
    :param service: service, defaults to None
    :type service: str, optional
    :param delay_ms: delay_ms, defaults to None
    :type delay_ms: int, optional
    """

    _model_class = ServerErrorResponseData
