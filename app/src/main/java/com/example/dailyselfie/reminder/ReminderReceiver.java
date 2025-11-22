package com.example.dailyselfie.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            // Khi thiết bị khởi động lại, lên lịch nhắc nhở
            Scheduler.scheduleDailyReminder(context);
        }
    }
}
