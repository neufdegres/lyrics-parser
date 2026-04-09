from pathlib import Path
from random import randint

DATA_DIR = Path(__file__).resolve().parents[2] / "data"
        
def get_deepl_apikey():
    filepath = DATA_DIR / "deepl"
    return filepath.read_text(encoding="utf-8")

def get_lyrics_sample():
    filepath = DATA_DIR / "lyrics.txt"
    return filepath.read_text(encoding="utf-8")
