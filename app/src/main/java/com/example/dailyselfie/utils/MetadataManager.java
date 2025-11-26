package com.example.dailyselfie.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.dailyselfie.model.PhotoMetadata;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MetadataManager {
    private static final String PREF_NAME = "photo_metadata";
    private static final String KEY_METADATA = "metadata_map";
    private static MetadataManager instance;
    private SharedPreferences prefs;
    private Gson gson;

    private MetadataManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static MetadataManager getInstance(Context context) {
        if (instance == null) {
            instance = new MetadataManager(context);
        }
        return instance;
    }

    public void saveMetadata(String photoPath, PhotoMetadata metadata) {
        Map<String, PhotoMetadata> map = getAllMetadata();
        map.put(photoPath, metadata);
        String json = gson.toJson(map);
        prefs.edit().putString(KEY_METADATA, json).apply();
    }

    public PhotoMetadata getMetadata(String photoPath) {
        Map<String, PhotoMetadata> map = getAllMetadata();
        return map.getOrDefault(photoPath, new PhotoMetadata());
    }

    public Map<String, PhotoMetadata> getAllMetadata() {
        String json = prefs.getString(KEY_METADATA, null);
        if (json == null) return new HashMap<>();
        Type type = new TypeToken<HashMap<String, PhotoMetadata>>(){}.getType();
        Map<String, PhotoMetadata> map = gson.fromJson(json, type);
        return map != null ? map : new HashMap<>();
    }

    public void removeMetadata(String photoPath) {
        Map<String, PhotoMetadata> map = getAllMetadata();
        map.remove(photoPath);
        String json = gson.toJson(map);
        prefs.edit().putString(KEY_METADATA, json).apply();
    }
}