package com.example.dailyselfie.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.dailyselfie.R;
import com.example.dailyselfie.ui.MainActivity;

public class ReminderWorker extends Worker {

    private static final String CHANNEL_ID = "selfie_reminder";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        if (!PreferenceHelper.hasTakenPhotoToday(getApplicationContext())) {
            sendNotification();
        }

        // Lịch ngày tiếp theo
        Scheduler.scheduleDailyReminder(getApplicationContext());

        return Result.success();
    }

    private void sendNotification() {
        Context ctx = getApplicationContext();

        Intent intent = new Intent(ctx, MainActivity.class);
        intent.putExtra("openCamera", true);

        PendingIntent pi = PendingIntent.getActivity(
                ctx, 0, intent, PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager nm =
                (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel ch = new NotificationChannel(
                    CHANNEL_ID, "Selfie Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            nm.createNotificationChannel(ch);
        }

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle("Nhắc Selfie")
                .setContentText("Hôm nay bạn chưa chụp ảnh!")
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pi);

        nm.notify(999, b.build());
    }
}
