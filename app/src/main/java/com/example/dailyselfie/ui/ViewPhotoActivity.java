package com.example.dailyselfie.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.github.chrisbanes.photoview.PhotoView;
import com.bumptech.glide.Glide;
import com.example.dailyselfie.R;

import java.io.File;

public class ViewPhotoActivity extends AppCompatActivity {

    PhotoView fullImage;
    ImageButton btnBack;
    ImageButton btnDelete;
    String currentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(R.layout.activity_view_photo);

        fullImage = findViewById(R.id.fullImage);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);

        currentPhotoPath = getIntent().getStringExtra("path");

        if (currentPhotoPath != null) {
            File file = new File(currentPhotoPath);
            if (file.exists()) {
                Glide.with(this)
                        .load(file)
                        .into(fullImage);
            } else {
                Toast.makeText(this, "File ảnh không tồn tại", Toast.LENGTH_SHORT).show();
            }
        }

        btnBack.setOnClickListener(v -> finish());

        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa ảnh")
                .setMessage("Bạn có chắc chắn muốn xóa ảnh này không?")
                .setPositiveButton("Xóa", (dialog, which) -> performDelete())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDelete() {
        if (currentPhotoPath != null) {
            File file = new File(currentPhotoPath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    Toast.makeText(this, "Đã xóa ảnh", Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Toast.makeText(this, "Lỗi: Không thể xóa file", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}