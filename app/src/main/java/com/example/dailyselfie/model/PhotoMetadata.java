package com.example.dailyselfie.model;

import java.util.HashMap;
import java.util.Map;

public class PhotoMetadata {
    public String note = "";
    public String emoji = "heart"; // mặc định trái tim

    public PhotoMetadata() {}

    public PhotoMetadata(String note, String emoji) {
        this.note = note;
        this.emoji = emoji;
    }

    // Để lưu vào SharedPreferences
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("note", note);
        map.put("emoji", emoji);
        return map;
    }

    public static PhotoMetadata fromMap(Map<String, Object> map) {
        PhotoMetadata m = new PhotoMetadata();
        if (map.containsKey("note")) m.note = (String) map.get("note");
        if (map.containsKey("emoji")) m.emoji = (String) map.get("emoji");
        return m;
    }
}