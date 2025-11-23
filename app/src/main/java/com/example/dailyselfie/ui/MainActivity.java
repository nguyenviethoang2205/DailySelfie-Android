package com.example.dailyselfie.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dailyselfie.R;
import com.example.dailyselfie.model.PhotoItem;
import com.example.dailyselfie.reminder.Scheduler;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<PhotoItem> photoItems;
    PhotoAdapter photoAdapter;

    private ActivityResultLauncher<Intent> cameraLauncher;

    CardView layoutOnThisDay;
    ImageView imgOnThisDay;
    TextView txtOnThisDayYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(R.layout.activity_main);
        Scheduler.scheduleDailyReminder(this);

        layoutOnThisDay = findViewById(R.id.layoutOnThisDay);
        imgOnThisDay = findViewById(R.id.imgOnThisDay);
        txtOnThisDayYear = findViewById(R.id.txtOnThisDayYear);
        ImageView btnSettings = findViewById(R.id.btnSetting);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabCamera = findViewById(R.id.fabCamera);

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuSettingsActivity.class);
            startActivity(intent);
        });

        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        );
        photoItems = new ArrayList<>();
        photoAdapter = new PhotoAdapter(photoItems, file -> {
            Intent intent = new Intent(MainActivity.this, ViewPhotoActivity.class);
            intent.putExtra("path", file.getAbsolutePath());
            startActivity(intent);
        });
        recyclerView.setAdapter(photoAdapter);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        reloadPhotoList();
                        checkForOnThisDay();
                        recyclerView.scrollToPosition(0);
                        Toast.makeText(this, "Đã lưu ảnh mới!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        fabCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            cameraLauncher.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadPhotoList();
        checkForOnThisDay();
    }

    private void checkForOnThisDay() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null || dir.listFiles() == null) {
            layoutOnThisDay.setVisibility(View.GONE);
            return;
        }
        File[] files = dir.listFiles();
        Calendar now = Calendar.getInstance();
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);
        File matchedFile = null;
        int yearDiff = 0;
        for (File file : files) {
            Calendar fileCal = Calendar.getInstance();
            fileCal.setTimeInMillis(file.lastModified());
            int fDay = fileCal.get(Calendar.DAY_OF_MONTH);
            int fMonth = fileCal.get(Calendar.MONTH);
            int fYear = fileCal.get(Calendar.YEAR);
            if (fDay == currentDay && fMonth == currentMonth && fYear < currentYear) {
                matchedFile = file;
                yearDiff = currentYear - fYear;
                break;
            }
        }
        if (matchedFile != null) {
            layoutOnThisDay.setVisibility(View.VISIBLE);
            Glide.with(this).load(matchedFile).into(imgOnThisDay);
            txtOnThisDayYear.setText(yearDiff + " năm trước");
            File finalMatchedFile = matchedFile;
            imgOnThisDay.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ViewPhotoActivity.class);
                intent.putExtra("path", finalMatchedFile.getAbsolutePath());
                startActivity(intent);
            });
        } else {
            layoutOnThisDay.setVisibility(View.GONE);
        }
    }

    private void reloadPhotoList() {
        if (photoItems != null && photoAdapter != null) {
            photoItems.clear();
            photoItems.addAll(loadGroupedPhotos());
            photoAdapter.notifyDataSetChanged();
        }
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