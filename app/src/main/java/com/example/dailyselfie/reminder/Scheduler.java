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
        Calendar reminder = Calendar.getInstance();
        reminder.set(Calendar.HOUR_OF_DAY, hour);
        reminder.set(Calendar.MINUTE, minute);
        reminder.set(Calendar.SECOND, 0);

        long delay = reminder.getTimeInMillis() - now.getTimeInMillis();
        if (delay < 0) delay += TimeUnit.DAYS.toMillis(1);

        OneTimeWorkRequest req =
                new OneTimeWorkRequest.Builder(ReminderWorker.class)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .addTag("daily_selfie_reminder")
                        .build();

        WorkManager.getInstance(context).enqueueUniqueWork(
                "daily_selfie_reminder",
                ExistingWorkPolicy.REPLACE,
                req
        );
    }
}
