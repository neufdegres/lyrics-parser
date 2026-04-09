import app.services.util as util
from app.models.lyrics import Lyrics

def import_lyrics() :
    lyrics = util.get_lyrics_sample().split("----------\n")[1:]
    
    if lyrics == None or len(lyrics) == 0 :
        return False, None
    
    res : list[Lyrics] = []
    
    for l in lyrics :
        lines = l.split("\n")
        tmp = Lyrics(title=lines[0], artist=lines[1], lines=lines[3:])
        res.append(tmp)
        
    return True, res