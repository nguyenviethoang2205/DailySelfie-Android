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

        findViewById(R.id.rowTimelapse).setOnClickListener(v ->
                Toast.makeText(this, "Đang phát triển", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.rowBackup).setOnClickListener(v ->
                Toast.makeText(this, "Sao lưu", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.rowSecurity).setOnClickListener(v ->
                Toast.makeText(this, "Cài đặt bảo mật", Toast.LENGTH_SHORT).show()
        );

    }
}
