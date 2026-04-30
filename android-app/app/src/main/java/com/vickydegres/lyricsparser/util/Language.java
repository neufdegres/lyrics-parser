package com.vickydegres.lyricsparser.util;

import androidx.annotation.NonNull;

public class Language {
    private final String code;
    private final String name;
    private final int flag;

    public Language(String code, String name, int flag) {
        this.code = code;
        this.name = name;
        this.flag = flag;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public int getFlag() {
        return flag;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }
}
