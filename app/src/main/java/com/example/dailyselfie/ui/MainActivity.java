package com.example.dailyselfie.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;


import com.example.dailyselfie.R;
import com.example.dailyselfie.model.PhotoItem;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import android.widget.ImageView;
public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<PhotoItem> photoItems;          // danh sách ảnh
    PhotoAdapter photoAdapter;
    private ActivityResultLauncher<Intent> cameraLauncher;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ẩn status bar → fullscreen
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                       View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(R.layout.activity_main);
        //Thêm xử lý nút Settings
        ImageView btnSettings = findViewById(R.id.btnSetting);
        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuSettingsActivity.class);
            startActivity(intent);
        });

        // Tìm RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        );

        // Khởi tạo launcher để nhận kết quả từ CameraActivity
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        // reload ảnh mới
                        photoItems.clear();
                        photoItems.addAll(loadGroupedPhotos());
                        photoAdapter.notifyDataSetChanged();
                        // scroll lên đầu để thấy ảnh mới vừa chụp
                        recyclerView.scrollToPosition(0);
                    }
                }
        );

        // Tìm FAB
        FloatingActionButton fabCamera = findViewById(R.id.fabCamera);
        fabCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            cameraLauncher.launch(intent);  // mở CameraActivity
        });

        // Load danh sách ảnh và gắn adapter
        photoItems = loadGroupedPhotos();
        photoAdapter = new PhotoAdapter(photoItems, file -> {
            Intent intent = new Intent(MainActivity.this, ViewPhotoActivity.class);
            intent.putExtra("path", file.getAbsolutePath());
            startActivity(intent);
        });
        recyclerView.setAdapter(photoAdapter);
    }



    private List<PhotoItem> loadGroupedPhotos() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null || dir.listFiles() == null) return Collections.emptyList();

        File[] files = dir.listFiles();
        Arrays.sort(files, (o1, o2) -> Long.compare(o2.lastModified(), o1.lastModified()));

        List<PhotoItem> items = new ArrayList<>();

        String lastDate = "";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        for (File file : files) {
            String date = sdf.format(new Date(file.lastModified()));
            if (!date.equals(lastDate)) {
                items.add(PhotoItem.createDate(date));
                lastDate = date;
            }
            items.add(PhotoItem.createPhoto(file));
        }

        return items;
    }

}
