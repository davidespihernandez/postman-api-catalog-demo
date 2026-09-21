from __future__ import annotations
from typing import List
from pydantic import Field
from typing import Optional
from typing import Any
from .utils.base_model import BaseModel


class Info(BaseModel):
    """Info

    :param title: title
    :type title: str
    :param version: version
    :type version: str
    :param description: description, defaults to None
    :type description: str, optional
    """

    title: str
    version: str
    description: Optional[str] = Field(default=None)


class Servers(BaseModel):
    """Servers

    :param url: url, defaults to None
    :type url: str, optional
    :param description: description, defaults to None
    :type description: str, optional
    """

    url: Optional[str] = Field(default=None)
    description: Optional[str] = Field(default=None)


class OpenApiDocument(BaseModel):
    """OpenAPI 3.0 document

    :param openapi: openapi
    :type openapi: str
    :param info: info
    :type info: Info
    :param servers: servers, defaults to None
    :type servers: List[Servers], optional
    :param paths: paths
    :type paths: dict
    :param components: components, defaults to None
    :type components: dict, optional
    """

    openapi: str
    info: Info
    servers: Optional[List[Servers]] = Field(default=None)
    paths: dict
    components: Optional[dict] = Field(default=None)
