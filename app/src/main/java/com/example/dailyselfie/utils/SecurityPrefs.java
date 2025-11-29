package com.example.dailyselfie.utils; // Hoặc package của bạn

import android.content.Context;
import android.content.SharedPreferences;

public class SecurityPrefs {
    private static final String PREF_NAME = "security_prefs";
    private static final String KEY_PIN = "app_pin";
    private static final String KEY_ENABLED = "is_security_enabled";

    public static void setPin(Context context, String pin) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PIN, pin).putBoolean(KEY_ENABLED, true).apply();
    }

    public static String getPin(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_PIN, "");
    }

    public static boolean isSecurityEnabled(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(KEY_ENABLED, false);
    }

    public static void clearSecurity(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}