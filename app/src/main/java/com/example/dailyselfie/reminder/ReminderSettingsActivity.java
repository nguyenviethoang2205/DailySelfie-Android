package com.example.dailyselfie.reminder;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TimePicker;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyselfie.R;

public class ReminderSettingsActivity extends AppCompatActivity {

    private TimePicker timePicker;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_settings);

        timePicker = findViewById(R.id.timePicker);
        Button btnSave = findViewById(R.id.btnSaveReminder);
        Button btnCancel = findViewById(R.id.btnCancelReminder);

        timePicker.setIs24HourView(true);

        // Load time đã lưu trước đó
        SharedPreferences pref = getSharedPreferences("reminder_prefs", MODE_PRIVATE);
        int savedHour = pref.getInt("hour", 8);
        int savedMinute = pref.getInt("minute", 0);

        timePicker.setHour(savedHour);
        timePicker.setMinute(savedMinute);

        // SAVE
        btnSave.setOnClickListener(v -> {
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();

            // Lưu lại thời gian
            SharedPreferences.Editor editor = pref.edit();
            editor.putInt("hour", hour);
            editor.putInt("minute", minute);
            editor.apply();

            // Đặt lịch lại bằng WorkManager
            ReminderScheduler.scheduleDailyReminder(this, hour, minute);

            finish();
        });

        // CANCEL
        btnCancel.setOnClickListener(v -> finish());
    }
}
