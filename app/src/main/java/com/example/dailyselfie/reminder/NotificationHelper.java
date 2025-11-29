package com.example.dailyselfie.reminder;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import androidx.core.app.NotificationCompat;

import com.example.dailyselfie.R;

public class NotificationHelper {

    private static final String CHANNEL_ID = "selfie_reminder_channel";

    public static void sendNotification(Context context) {

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Nhắc nhở chụp ảnh",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setContentTitle("Nhắc chụp ảnh selfie")
                        .setContentText("Hôm nay bạn chưa chụp ảnh! Nhớ chụp 1 tấm nha 😊")
                        .setSmallIcon(android.R.drawable.ic_menu_camera)
                        .setAutoCancel(true);

        manager.notify(1001, builder.build());
    }
}
