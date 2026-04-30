package com.vickydegres.lyricsparser.data;

import android.content.Context;

import com.vickydegres.lyricsparser.util.Func;
import com.vickydegres.lyricsparser.util.Language;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class LanguageRepository {
    private static final Map<String, Language> languages = new HashMap<>();

    public static void load(Context context) {
        try {
            InputStream is = context.getAssets().open("languages.json");
            String raw = Func.readStream(is);

            JSONArray arr = new JSONArray(raw);

            for (int i=0; i< arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String code = obj.getString("code");
                String name = obj.getString("name");

                int flagId = context.getResources().getIdentifier(
                        code.toLowerCase(), "drawable", context.getPackageName());

                languages.put(code, new Language(code, name, flagId));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Language[] getAll() {
        return languages.values().toArray(new Language[0]);
    }

    public static Language get(String code) {
        return languages.getOrDefault(code, languages.get("JP"));
    }
}
