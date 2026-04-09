from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from app.services.translation import translate_v2
from app.services.romanization import romanize
from app.services.sample import import_lyrics
from app.models.requests import TranslationRequest, RomanizationRequest

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
)

@app.get("/")
async def root():
    return {"message": "LyricsParser API is running !"}

@app.post("/translation")
async def translate_lyrics(req: TranslationRequest):
    if req.lines == None or len(req.lines) == 0 :
        raise HTTPException(status_code=400, detail="Pas de texte à traduire")
    
    done, tra_lines = translate_v2(req.lines, req.lang)
    
    if not done :
        raise HTTPException(status_code=500, detail="Erreur lors de la traduction")
    
    return {"lines" : tra_lines}

@app.post("/romanization")
async def romanize_lyrics(req: RomanizationRequest):
    if req.lines == None or len(req.lines) == 0 :
        raise HTTPException(status_code=400, detail="Pas de texte à romaniser")
    
    done, rom_lines = romanize(req.lines, req.lang)
    
    if not done :
        raise HTTPException(status_code=500, detail="Erreur lors de la romanisation")
    
    return {"lines" : rom_lines}

@app.get("/sample")
async def get_lyrics_sample() :
    done, lyrics = import_lyrics()
    
    if not done :
        raise HTTPException(status_code=500, detail="Erreur lors de l'import des lyrics (serveur)'")
    
    return {"lyrics" : lyrics}