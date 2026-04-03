from app.services.detect_lang import detect_ja
from cutlet import Cutlet
from unicodedata import normalize

def link(items : list):
    res = ""
    for i in range(len(items)) :
        if i != 0  :
            curr_lang = items[i]["lang"]
            if items[i-0]["lang"] == "ja" :
                if curr_lang == "ja" :
                    res += " "
            else :
                if curr_lang == "ja" :
                    res += " "
                    
        res += items[i]["text"]    
        res += " "
    return res
        
        
def romanize(lines: list[str], lang: str = "jp") :    
    try :
        cut = Cutlet(use_foreign_spelling=False)
        
        rom = []
        
        for l in lines :
            raw = normalize('NFKC', l)
            tab = raw.split(" ")
            sub_res = []
            for t in tab :
                is_ja = detect_ja(t)
                if not is_ja :
                    sub_res.append({"lang" : "else", "text": t})
                else :
                    text = cut.romaji(t).lower()
                    sub_res.append({"lang" : "ja", "text": text})            
            res = link(sub_res).strip()

            rom.append(res)
                    
        return True, rom 
        
    except Exception as e :
        # print(repr(e))
        return False, None