package com.example.app;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String PREFS_NAME = "TextPolisherPrefs";
    private static final String API_KEY = "openai_api_key";
    
    private SharedPreferences prefs;
    
    public PreferencesManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    public void saveApiKey(String apiKey) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(API_KEY, apiKey);
        editor.apply();
    }
    
    public String getApiKey() {
        return prefs.getString(API_KEY, "");
    }
    
    public boolean hasApiKey() {
        String apiKey = getApiKey();
        return apiKey != null && !apiKey.trim().isEmpty();
    }
    
    public void clearApiKey() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(API_KEY);
        editor.apply();
    }
}