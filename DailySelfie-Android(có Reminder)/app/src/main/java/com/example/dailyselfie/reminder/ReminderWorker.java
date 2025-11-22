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

import com.example.dailyselfie.ui.MainActivity;
import com.example.dailyselfie.R;

public class ReminderWorker extends Worker {

    private static final String CHANNEL_ID = "selfie_reminder_channel";

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Kiểm tra xem người dùng đã chụp ảnh hôm nay chưa
        boolean hasTakenPhotoToday = PreferenceHelper.hasTakenPhotoToday(getApplicationContext());

        if (!hasTakenPhotoToday) {
            sendNotification();
        }

        // Lên lịch nhắc nhở cho ngày tiếp theo
        Scheduler.scheduleDailyReminder(getApplicationContext());

        return Result.success();
    }

    private void sendNotification() {
        Context context = getApplicationContext();

        Intent intent = new Intent(context, MainActivity.class);
        intent.putExtra("openCamera", true); // Khi mở app sẽ mở camera
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Selfie Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription("Nhắc nhở chụp selfie hàng ngày");
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_camera)
                .setContentTitle("Nhắc nhở Selfie")
                .setContentText("Hôm nay bạn chưa chụp ảnh selfie. Chụp ngay thôi!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        notificationManager.notify(1001, builder.build());
    }
}
