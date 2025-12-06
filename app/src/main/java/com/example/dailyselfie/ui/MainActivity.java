package com.example.dailyselfie.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.dailyselfie.R;
import com.example.dailyselfie.model.PhotoItem;
import com.example.dailyselfie.reminder.ReminderScheduler;
import com.example.dailyselfie.reminder.SelfieTracker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    List<PhotoItem> photoItems;
    PhotoAdapter photoAdapter;

    private ActivityResultLauncher<Intent> cameraLauncher;

    CardView layoutOnThisDay;
    ImageView imgOnThisDay;
    TextView txtOnThisDayYear;

    public static Map<String, String> photoNotes = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean isUnlocked = getIntent().getBooleanExtra("IS_UNLOCKED", false);

        if (com.example.dailyselfie.utils.SecurityPrefs.isSecurityEnabled(this) && !isUnlocked) {
            Intent intent = new Intent(this, PinActivity.class);
            intent.putExtra("SETUP_MODE", false);
            startActivity(intent);
            finish();
            return;
        }

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        );

        setContentView(R.layout.activity_main);

        SharedPreferences pref = getSharedPreferences("reminder_prefs", MODE_PRIVATE);
        int hour = pref.getInt("hour", 8);   // default 08:00
        int minute = pref.getInt("minute", 0);

        ReminderScheduler.scheduleDailyReminder(this, hour, minute);
        layoutOnThisDay = findViewById(R.id.layoutOnThisDay);
        imgOnThisDay = findViewById(R.id.imgOnThisDay);
        txtOnThisDayYear = findViewById(R.id.txtOnThisDayYear);
        ImageView btnSettings = findViewById(R.id.btnSetting);
        recyclerView = findViewById(R.id.recyclerView);
        FloatingActionButton fabCamera = findViewById(R.id.fabCamera);
        ImageButton btnSelectAll = findViewById(R.id.btnSelectAll);
        ImageButton btnCancelSelect = findViewById(R.id.btnCancelSelect);

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuSettingsActivity.class);
            startActivity(intent);
        });

        btnSelectAll.setOnClickListener(v -> {
            photoAdapter.selectedPhotos.clear();
            for (PhotoItem item : photoItems) {
                if (item.type == PhotoItem.TYPE_PHOTO) {  // chỉ chọn ảnh, bỏ qua header ngày
                    photoAdapter.selectedPhotos.add(item.file.getAbsolutePath());
                }
            }
            photoAdapter.isSelectMode = true;
            photoAdapter.notifyDataSetChanged();
            updateTitle();
        });

        btnCancelSelect.setOnClickListener(v -> {
            photoAdapter.exitSelectMode();
            updateTitle();
        });

        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        );
        photoItems = new ArrayList<>();

        loadNotesFromStorage();


        photoAdapter = new PhotoAdapter(photoItems, new PhotoAdapter.OnItemClick() {
            @Override
            public void onClick(File fileClicked) {
                if (photoAdapter.isSelectMode) {
                    photoAdapter.toggleSelect(fileClicked.getAbsolutePath());
                    updateTitle();
                } else {

                    ArrayList<String> allPaths = new ArrayList<>();
                    int targetPosition = 0;
                    int index = 0;

                    for (PhotoItem item : photoItems) {
                        if (item.type == PhotoItem.TYPE_PHOTO) {
                            String path = item.file.getAbsolutePath();
                            allPaths.add(path);

                            if (path.equals(fileClicked.getAbsolutePath())) {
                                targetPosition = index;
                            }
                            index++;
                        }
                    }

                    Intent intent = new Intent(MainActivity.this, ViewPhotoActivity.class);
                    intent.putStringArrayListExtra("list_paths", allPaths);
                    intent.putExtra("position", targetPosition);
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(File file) {
                photoAdapter.startSelectMode(file.getAbsolutePath());
                updateTitle();
            }

            @Override
            public void onAddNote(File file) {
                showNoteDialog(file);
            }
        });

        recyclerView.setAdapter(photoAdapter);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        reloadPhotoList();
                        checkForOnThisDay();
                        recyclerView.scrollToPosition(0);

                        // Đánh dấu hôm nay đã chụp
                        SelfieTracker.markToday(this);
                    }
                }
        );

        fabCamera.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, CameraActivity.class);
            cameraLauncher.launch(intent);
        });

        boolean openCameraNow = getIntent().getBooleanExtra("OPEN_CAMERA_IMMEDIATELY", false);
        if (openCameraNow) {
            Intent intent = new Intent(MainActivity.this, com.example.dailyselfie.ui.CameraActivity.class);
            cameraLauncher.launch(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        reloadPhotoList();
        checkForOnThisDay();
        updateTitle();
    }

    private void updateTitle() {
        int count = photoAdapter.getSelectedCount();
        TextView txtHeader = findViewById(R.id.txtHeader);
        ImageButton btnSetting = findViewById(R.id.btnSetting);
        ImageButton btnSelectAll = findViewById(R.id.btnSelectAll);
        ImageButton btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        ImageButton btnCancelSelect = findViewById(R.id.btnCancelSelect);

        if (count > 0) {
            txtHeader.setText("Đã chọn " + count);
            btnSetting.setVisibility(View.GONE);
            btnSelectAll.setVisibility(View.VISIBLE);
            btnDeleteSelected.setVisibility(View.VISIBLE);
            btnCancelSelect.setVisibility(View.VISIBLE);
        } else {
            txtHeader.setText("Nhật ký Selfie");
            btnSetting.setVisibility(View.VISIBLE);
            btnSelectAll.setVisibility(View.GONE);
            btnDeleteSelected.setVisibility(View.GONE);
            btnCancelSelect.setVisibility(View.GONE);
            photoAdapter.exitSelectMode();
        }
    }

    private void showNoteDialog(File file) {
        String path = file.getAbsolutePath();
        String currentNote = photoNotes.getOrDefault(path, "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ghi chú cho ảnh");

        EditText input = new EditText(this);
        input.setHint("Ví dụ: Happy today");
        input.setText(currentNote);
        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String note = input.getText().toString().trim();

            if (note.isEmpty()) {
                photoNotes.remove(path);
            } else {
                photoNotes.put(path, note);
            }

            saveNoteToStorage(path, note);

            photoAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Hủy", null);

        builder.setNeutralButton("Xóa ghi chú", (dialog, which) -> {
            photoNotes.remove(path);
            saveNoteToStorage(path, null);
            photoAdapter.notifyDataSetChanged();
        });

        builder.show();
    }

    public void deleteSelectedPhotos(View view) {
        int count = photoAdapter.getSelectedCount();
        if (count == 0) {
            Toast.makeText(this, "Chưa chọn ảnh nào", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Xóa " + count + " ảnh?")
                .setMessage("Ảnh sẽ bị xóa vĩnh viễn")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    for (String path : photoAdapter.selectedPhotos) {
                        new File(path).delete();
                        photoNotes.remove(path);
                        saveNoteToStorage(path, null);
                    }
                    photoAdapter.exitSelectMode();
                    reloadPhotoList();
                    updateTitle();
                    Toast.makeText(this, "Đã xóa " + count + " ảnh", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void checkForOnThisDay() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null || dir.listFiles() == null) {
            layoutOnThisDay.setVisibility(View.GONE);
            return;
        }

        File[] files = dir.listFiles();
        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));

        Calendar now = Calendar.getInstance();
        int currentDay = now.get(Calendar.DAY_OF_MONTH);
        int currentMonth = now.get(Calendar.MONTH);
        int currentYear = now.get(Calendar.YEAR);

        ArrayList<String> memoryPaths = new ArrayList<>();
        int yearDiff = 0;

        for (File file : files) {
            Calendar fileCal = Calendar.getInstance();
            fileCal.setTimeInMillis(file.lastModified());

            int fDay = fileCal.get(Calendar.DAY_OF_MONTH);
            int fMonth = fileCal.get(Calendar.MONTH);
            int fYear = fileCal.get(Calendar.YEAR);

            if (fDay == currentDay && fMonth == currentMonth && fYear < currentYear) {
                memoryPaths.add(file.getAbsolutePath());

                if (yearDiff == 0) {
                    yearDiff = currentYear - fYear;
                }
            }
        }

        if (!memoryPaths.isEmpty()) {
            layoutOnThisDay.setVisibility(View.VISIBLE);

            File coverPhoto = new File(memoryPaths.get(0));
            Glide.with(this).load(coverPhoto).into(imgOnThisDay);

            String infoText = yearDiff + " năm trước";
            if (memoryPaths.size() > 1) {
                infoText = "+" + memoryPaths.size() + " ảnh • " + infoText;
            }
            txtOnThisDayYear.setText(infoText);

            imgOnThisDay.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ViewPhotoActivity.class);

                intent.putStringArrayListExtra("list_paths", memoryPaths);

                intent.putExtra("position", 0);

                startActivity(intent);
            });

        } else {
            layoutOnThisDay.setVisibility(View.GONE);
        }
    }

    private void reloadPhotoList() {
        photoItems.clear();
        photoItems.addAll(loadGroupedPhotos());
        photoAdapter.notifyDataSetChanged();
    }

    private List<PhotoItem> loadGroupedPhotos() {
        File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null || !dir.exists()) return Collections.emptyList();
        File[] files = dir.listFiles();
        if (files == null) return Collections.emptyList();

        Arrays.sort(files, (a, b) -> Long.compare(b.lastModified(), a.lastModified()));
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

    private void loadNotesFromStorage() {
        SharedPreferences prefs = getSharedPreferences("my_photo_notes", MODE_PRIVATE);
        java.util.Map<String, ?> allNotes = prefs.getAll();

        photoNotes.clear();
        for (java.util.Map.Entry<String, ?> entry : allNotes.entrySet()) {
            if (entry.getValue() instanceof String) {
                photoNotes.put(entry.getKey(), (String) entry.getValue());
            }
        }
    }

    private void saveNoteToStorage(String path, String note) {
        SharedPreferences prefs = getSharedPreferences("my_photo_notes", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (note == null || note.isEmpty()) {
            editor.remove(path);
        } else {
            editor.putString(path, note);
        }
        editor.apply();
    }
}