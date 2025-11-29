package com.example.dailyselfie.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.dailyselfie.R;
import com.example.dailyselfie.utils.BackupUtils;

import java.io.File;

public class BackupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        Button btnBackup = findViewById(R.id.btnBackup);
        Button btnRestore = findViewById(R.id.btnRestore);

        btnBackup.setOnClickListener(v -> {
            Toast.makeText(this, "Đang nén dữ liệu...", Toast.LENGTH_SHORT).show();

            new Thread(() -> {
                File zipFile = BackupUtils.createBackupZip(this);
                if (zipFile != null && zipFile.exists()) {
                    runOnUiThread(() -> shareZipFile(zipFile));
                } else {
                    runOnUiThread(() -> Toast.makeText(this, "Lỗi khi tạo file backup", Toast.LENGTH_SHORT).show());
                }
            }).start();
        });

        ActivityResultLauncher<String> filePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        boolean success = BackupUtils.restoreFromZip(this, uri);
                        if (success) {
                            Toast.makeText(this, "Khôi phục thành công! Hãy khởi động lại App.", Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(this, "File lỗi hoặc không đúng định dạng.", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
        );

        btnRestore.setOnClickListener(v -> {
            filePicker.launch("application/zip");
        });
    }

    private void shareZipFile(File file) {
        try {
            Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("application/zip");
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            startActivity(Intent.createChooser(intent, "Lưu file Backup vào..."));

        } catch (Exception e) {
            Toast.makeText(this, "Lỗi chia sẻ: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}