package com.example.dailyselfie.ui;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.content.Intent;
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
import com.example.dailyselfie.reminder.Scheduler;
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

    // Biến lưu ghi chú (đường dẫn -> ghi chú + emoji)
    public static Map<String, String> photoNotes = new HashMap<>();

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
        ImageButton btnSelectAll = findViewById(R.id.btnSelectAll);
        ImageButton btnDeleteSelected = findViewById(R.id.btnDeleteSelected);
        ImageButton btnCancelSelect = findViewById(R.id.btnCancelSelect);
        TextView txtHeader = findViewById(R.id.txtHeader);

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MenuSettingsActivity.class);
            startActivity(intent);
        });

        // Nút chọn nhiều
        btnSelectAll.setOnClickListener(v -> {
            photoAdapter.startSelectMode(null); // bật chế độ chọn
            updateTitle();
        });

        // Nút hủy chọn
        btnCancelSelect.setOnClickListener(v -> {
            photoAdapter.exitSelectMode();
            updateTitle();
        });

        recyclerView.setLayoutManager(
                new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL)
        );
        photoItems = new ArrayList<>();

        // Tạo adapter với đầy đủ chức năng
        photoAdapter = new PhotoAdapter(photoItems, new PhotoAdapter.OnItemClick() {
            @Override
            public void onClick(File file) {
                if (photoAdapter.isSelectMode) {
                    photoAdapter.toggleSelect(file.getAbsolutePath());
                } else {
                    // Xem ảnh lớn
                    Intent intent = new Intent(MainActivity.this, ViewPhotoActivity.class);
                    intent.putExtra("path", file.getAbsolutePath());
                    startActivity(intent);
                }
            }

            @Override
            public void onLongClick(File file) {
                photoAdapter.startSelectMode(file.getAbsolutePath());
                updateTitle(); // hiện số ảnh đã chọn
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
        updateTitle();
    }

    // Cập nhật tiêu đề: hiện số ảnh đang chọn
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

    // Dialog thêm ghi chú + emoji
    private void showNoteDialog(File file) {
        String path = file.getAbsolutePath();
        String currentNote = photoNotes.getOrDefault(path, "");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Ghi chú cho ảnh");

        View view = getLayoutInflater().inflate(android.R.layout.simple_list_item_1, null);
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
            photoAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Hủy", null);
        builder.setNeutralButton("Xóa ghi chú", (dialog, which) -> {
            photoNotes.remove(path);
            photoAdapter.notifyDataSetChanged();
        });

        builder.show();
    }

    // Nút XÓA NHIỀU ẢNH (bạn thêm nút này vào toolbar hoặc menu đều được)
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
                    }
                    photoAdapter.exitSelectMode();
                    reloadPhotoList();
                    updateTitle();
                    Toast.makeText(this, "Đã xóa " + count + " ảnh", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    // Các hàm cũ giữ nguyên
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
}