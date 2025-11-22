package com.example.dailyselfie.reminder;

import android.os.Bundle;
import android.widget.TimePicker;
import androidx.appcompat.app.AppCompatActivity;
import com.example.dailyselfie.R;
import com.example.dailyselfie.reminder.PreferenceHelper;
import com.example.dailyselfie.reminder.Scheduler;

public class ReminderSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        TimePicker timePicker = findViewById(R.id.timePicker);

        // Thiết lập giờ hiện tại từ SharedPreferences
        int hour = PreferenceHelper.getReminderHour(this);
        int minute = PreferenceHelper.getReminderMinute(this);
        timePicker.setHour(hour);
        timePicker.setMinute(minute);

        // Lưu thay đổi khi người dùng chỉnh giờ
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute1) -> {
            PreferenceHelper.setReminderTime(this, hourOfDay, minute1);
            Scheduler.scheduleDailyReminder(this); // Lên lịch lại nhắc nhở
        });
    }
}
