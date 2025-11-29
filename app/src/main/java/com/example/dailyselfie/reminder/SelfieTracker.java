package com.example.dailyselfie.reminder;

import android.content.Context;
import android.content.SharedPreferences;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SelfieTracker {

    private static final String PREFS_NAME = "selfie_tracker";

    // Đánh dấu hôm nay đã chụp
    public static void markToday(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        pref.edit().putBoolean(today, true).apply();
    }

    // Kiểm tra hôm nay đã chụp chưa
    public static boolean isTodayTaken(Context context) {
        SharedPreferences pref = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String today = new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date());
        return pref.getBoolean(today, false);
    }
}
