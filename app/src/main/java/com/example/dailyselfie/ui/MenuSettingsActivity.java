package com.example.dailyselfie.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dailyselfie.R;
import com.example.dailyselfie.reminder.ReminderSettingsActivity;

public class MenuSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_settings);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        findViewById(R.id.rowReminder).setOnClickListener(v -> {
            Intent i = new Intent(this, ReminderSettingsActivity.class);
            startActivity(i);
        });

        findViewById(R.id.rowBackup).setOnClickListener(v -> {
            Intent intent = new Intent(MenuSettingsActivity.this, BackupActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.rowSecurity).setOnClickListener(v -> {
            Intent intent = new Intent(MenuSettingsActivity.this, PinActivity.class);
            intent.putExtra("SETUP_MODE", true);
            startActivity(intent);
        });

        findViewById(R.id.rowTimelapse).setOnClickListener(v -> {
            Intent intent = new Intent(MenuSettingsActivity.this, TimeLapseActivity.class);
            startActivity(intent);
        });
    }
}