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
import com.example.dailyselfie.ui.CameraActivity;

public class ReminderWorker extends Worker {

    public ReminderWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        // Kiểm tra xem hôm nay đã chụp selfie chưa
        if (SelfieTracker.isTodayTaken(getApplicationContext())) {
            return Result.success(); // nếu đã chụp → không gửi notification
        }

        // Tạo Intent để mở CameraActivity khi nhấn notification
        Intent intent = new Intent(getApplicationContext(), CameraActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Tạo Notification
        String channelId = "reminder_channel";
        NotificationManager manager = (NotificationManager) getApplicationContext()
                .getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Nhắc nhở", NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), channelId)
                .setSmallIcon(R.drawable.ic_notification) // cần icon trong res/drawable
                .setContentTitle("Nhắc nhở selfie")
                .setContentText("Bạn chưa chụp ảnh selfie hôm nay!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent); // <-- quan trọng: mở CameraActivity khi nhấn

        manager.notify(1001, builder.build());

        return Result.success();
    }
}
