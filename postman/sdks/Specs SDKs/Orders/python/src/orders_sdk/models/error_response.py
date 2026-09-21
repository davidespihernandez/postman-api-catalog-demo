from __future__ import annotations
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_error import BaseError
from .utils.base_model import BaseModel


# Pydantic validation model for ErrorResponse
class ErrorResponseData(BaseModel):
    """ErrorResponse

    :param error: error
    :type error: str
    :param service: service, defaults to None
    :type service: str, optional
    """

    error: str
    service: Optional[str] = Field(default=None)


# Error exception class
class ErrorResponse(BaseError):
    """ErrorResponse

    :param error: error
    :type error: str
    :param service: service, defaults to None
    :type service: str, optional
    """

    _model_class = ErrorResponseData
