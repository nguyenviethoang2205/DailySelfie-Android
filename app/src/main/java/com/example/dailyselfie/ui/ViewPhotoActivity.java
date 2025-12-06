package com.example.dailyselfie.ui;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView; // Import TextView
import android.widget.Toast;

import com.example.dailyselfie.R;

import java.io.File;
import java.text.SimpleDateFormat; // Import để format ngày
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ViewPhotoActivity extends AppCompatActivity {

    ViewPager2 viewPager;
    ImageButton btnBack, btnDelete;
    TextView txtDateLabel;

    ArrayList<String> photoPaths;
    PhotoPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );
        setContentView(R.layout.activity_view_photo);

        viewPager = findViewById(R.id.viewPagerImage);
        btnBack = findViewById(R.id.btnBack);
        btnDelete = findViewById(R.id.btnDelete);
        txtDateLabel = findViewById(R.id.txtDateLabel); // [MỚI] Ánh xạ ID

        photoPaths = getIntent().getStringArrayListExtra("list_paths");
        int startPosition = getIntent().getIntExtra("position", 0);

        if (photoPaths == null || photoPaths.isEmpty()) {
            Toast.makeText(this, "Không có ảnh", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        adapter = new PhotoPagerAdapter(photoPaths);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(startPosition, false);

        updateDateDisplay(startPosition);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                updateDateDisplay(position);
            }
        });

        btnBack.setOnClickListener(v -> finish());
        btnDelete.setOnClickListener(v -> showDeleteConfirmation());
    }

    private void updateDateDisplay(int position) {
        if (position >= 0 && position < photoPaths.size()) {
            String path = photoPaths.get(position);
            File file = new File(path);
            if (file.exists()) {
                long lastModified = file.lastModified();
                Date date = new Date(lastModified);

                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                txtDateLabel.setText(sdf.format(date));
            }
        }
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Xóa ảnh")
                .setMessage("Bạn có chắc chắn muốn xóa ảnh đang xem không?")
                .setPositiveButton("Xóa", (dialog, which) -> performDelete())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void performDelete() {
        int currentPos = viewPager.getCurrentItem();

        if (currentPos >= 0 && currentPos < photoPaths.size()) {
            String path = photoPaths.get(currentPos);
            File file = new File(path);

            if (file.exists() && file.delete()) {
                photoPaths.remove(currentPos);
                adapter.notifyItemRemoved(currentPos);

                Toast.makeText(this, "Đã xóa", Toast.LENGTH_SHORT).show();

                if (photoPaths.isEmpty()) {
                    setResult(RESULT_OK);
                    finish();
                } else {

                    viewPager.post(() -> updateDateDisplay(viewPager.getCurrentItem()));
                }
            } else {
                Toast.makeText(this, "Lỗi khi xóa file", Toast.LENGTH_SHORT).show();
            }
        }
    }
}