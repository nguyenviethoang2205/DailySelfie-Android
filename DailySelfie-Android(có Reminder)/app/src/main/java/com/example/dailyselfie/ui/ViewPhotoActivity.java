package com.example.dailyselfie.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.example.dailyselfie.R;

import java.io.File;

public class ViewPhotoActivity extends AppCompatActivity {

    ImageView fullImage;
    ImageButton btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_photo);

        fullImage = findViewById(R.id.fullImage);
        btnBack = findViewById(R.id.btnBack);

        // Xử lý click nút back
        btnBack.setOnClickListener(v -> finish());

        // Hiển thị ảnh
        String path = getIntent().getStringExtra("path");
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                Glide.with(this).load(file).into(fullImage);
            }
        }
    }
}
