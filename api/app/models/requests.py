from pydantic import BaseModel

class TranslationRequest(BaseModel):
    lines: list[str]
    lang: str = "jp"

class RomanizationRequest(BaseModel):
    lines: list[str]
    lang: str = "jp"