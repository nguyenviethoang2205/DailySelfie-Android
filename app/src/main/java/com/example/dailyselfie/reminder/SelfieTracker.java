package com.example.dailyselfie.reminder;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SelfieTracker {

    public static boolean isTodayTaken(Context context) {
        File directory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (directory == null || !directory.exists()) {
            return false;
        }

        File[] files = directory.listFiles();
        if (files == null || files.length == 0) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String today = sdf.format(new Date());

        for (File file : files) {
            String fileDate = sdf.format(new Date(file.lastModified()));
            if (fileDate.equals(today)) {
                return true;
            }
        }

        return false;
    }

    public static void markToday(Context context) {
    }
}