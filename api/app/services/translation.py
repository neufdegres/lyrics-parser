import deepl
import app.services.util as util
from unicodedata import normalize

def translate(lines: list[str], lang: str = "jp") :
    try :
        auth_key = util.get_deepl_apikey()
        if auth_key.startswith("[") : raise Exception
        
        translator = deepl.Translator(auth_key)
        
        res = []
        
        for l in lines :
            ori = normalize('NFKC', l)
            if len(ori.strip()) == 0 :
                res.append("")
            else :
                res.append(translator.translate_text(ori, source_lang="JA", target_lang="EN-US").text)
                
        return True, res

    except Exception as e :
        print(repr(e))
        return False, None
    
def translate_v2(lines: list[str], lang: str = "jp") :
    try :
        auth_key = util.get_deepl_apikey()
        if auth_key.startswith("[") : raise Exception
        
        translator = deepl.Translator(auth_key)
        
        lines_set = set(lines)
        translated = dict()
        
        res = [""] * len(lines)
        
        for l in list(lines_set) :
            ori = normalize('NFKC', l)
            if len(ori.strip()) != 0 :
                translated[ori] = translator.translate_text(ori, source_lang="JA", target_lang="EN-US").text
                
        
        for i in range(len(lines)) :
            if len(lines[i].strip()) != 0 :
                res[i] = translated[normalize('NFKC', lines[i])]
            
        return True, res

    except Exception as e :
        print(repr(e))
        return False, None