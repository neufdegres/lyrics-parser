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
        # print(repr(e))
        return False, None