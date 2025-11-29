package com.example.dailyselfie.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.example.dailyselfie.R;
import com.example.dailyselfie.utils.TimeLapseHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TimeLapseActivity extends AppCompatActivity {

    private File outputVideoFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_lapse);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        SeekBar seekBar = findViewById(R.id.seekbarFps);
        TextView txtFps = findViewById(R.id.txtFpsValue);
        Button btnCreate = findViewById(R.id.btnCreateVideo);
        Button btnPlay = findViewById(R.id.btnPlayVideo);
        TextView txtStatus = findViewById(R.id.txtStatus);

        // Xử lý thanh kéo tốc độ
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int fps = Math.max(1, progress); // Tối thiểu 1 FPS
                txtFps.setText(fps + " khung hình/giây");
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // Xử lý nút Tạo Video
        btnCreate.setOnClickListener(v -> {
            txtStatus.setText("Đang xử lý... Vui lòng chờ!");
            btnCreate.setEnabled(false);

            int fps = Math.max(1, seekBar.getProgress());

            // Chạy tiến trình tạo video dưới nền (Background Thread)
            new Thread(() -> {
                // 1. Lấy danh sách ảnh
                File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                if (dir == null || dir.listFiles() == null) return;

                File[] files = dir.listFiles();
                List<File> imageFiles = new ArrayList<>(Arrays.asList(files));

                // Sắp xếp ảnh theo ngày cũ -> mới để video chạy đúng trình tự
                Collections.sort(imageFiles, (f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()));

                // 2. Tạo file video đầu ra
                outputVideoFile = new File(getExternalCacheDir(), "MySelfie_Timelapse.mp4");

                // 3. Gọi hàm tạo (Hàm này chạy lâu nên phải để trong Thread)
                File result = TimeLapseHelper.createTimeLapse(imageFiles, outputVideoFile, fps);

                // 4. Cập nhật giao diện khi xong
                runOnUiThread(() -> {
                    btnCreate.setEnabled(true);
                    if (result != null) {
                        txtStatus.setText("Đã tạo xong: " + result.getName());
                        btnPlay.setVisibility(android.view.View.VISIBLE);
                        Toast.makeText(this, "Video đã sẵn sàng!", Toast.LENGTH_SHORT).show();
                    } else {
                        txtStatus.setText("Lỗi khi tạo video");
                    }
                });
            }).start();
        });

        // Nút xem video
        btnPlay.setOnClickListener(v -> {
            if (outputVideoFile != null && outputVideoFile.exists()) {
                playVideo(outputVideoFile);
            }
        });
    }

    private void playVideo(File file) {
        Uri uri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(uri, "video/mp4");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(intent);
    }
}