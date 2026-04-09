from pydantic import BaseModel

class Lyrics(BaseModel):
    title: str
    artist: str
    lines: list[str]