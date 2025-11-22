package com.example.dailyselfie.reminder;

import android.content.Context;

import androidx.work.ExistingWorkPolicy;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class Scheduler {

    public static void scheduleDailyReminder(Context context) {
        int hour = PreferenceHelper.getReminderHour(context);
        int minute = PreferenceHelper.getReminderMinute(context);

        Calendar now = Calendar.getInstance();
        Calendar reminderTime = Calendar.getInstance();
        reminderTime.set(Calendar.HOUR_OF_DAY, hour);
        reminderTime.set(Calendar.MINUTE, minute);
        reminderTime.set(Calendar.SECOND, 0);

        long delay = reminderTime.getTimeInMillis() - now.getTimeInMillis();
        if (delay < 0) {
            delay += TimeUnit.DAYS.toMillis(1); // Lên lịch cho ngày mai nếu giờ đã qua
        }

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(ReminderWorker.class)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "daily_selfie_reminder",
                ExistingWorkPolicy.REPLACE,
                workRequest
        );
    }
}
