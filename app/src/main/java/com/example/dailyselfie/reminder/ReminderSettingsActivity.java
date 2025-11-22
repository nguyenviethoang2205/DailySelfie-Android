package com.example.dailyselfie.reminder;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyselfie.R;

public class ReminderSettingsActivity extends AppCompatActivity {

    private int tempHour, tempMinute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        TimePicker timePicker = findViewById(R.id.timePicker);
        Button btnSave = findViewById(R.id.btnSaveReminder);
        Button btnCancel = findViewById(R.id.btnCancelReminder);

        // Lấy giờ hiện tại từ PreferenceHelper
        tempHour = PreferenceHelper.getReminderHour(this);
        tempMinute = PreferenceHelper.getReminderMinute(this);

        timePicker.setHour(tempHour);
        timePicker.setMinute(tempMinute);

        // Lưu tạm khi chỉnh TimePicker
        timePicker.setOnTimeChangedListener((view, hourOfDay, minute) -> {
            tempHour = hourOfDay;
            tempMinute = minute;
        });

        // Nút Lưu
        btnSave.setOnClickListener(v -> {
            PreferenceHelper.setReminderTime(this, tempHour, tempMinute);
            Scheduler.scheduleDailyReminder(this); // lên lịch nhắc nhở
            Toast.makeText(this, "Đã lưu thời gian nhắc nhở", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Nút Hủy
        btnCancel.setOnClickListener(v -> finish());
    }
}
