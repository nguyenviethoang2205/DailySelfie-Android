package com.example.dailyselfie.ui;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.dailyselfie.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EditImageActivity extends AppCompatActivity {

    ImageView imgPreview;
    RecyclerView rvFilters;
    Button btnCropRotate, btnFilter, btnSave;
    ImageButton btnBack;

    Uri sourceUri, currentUri;
    String originalPath;

    private int currentFilterType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_image);

        imgPreview = findViewById(R.id.imgPreview);
        rvFilters = findViewById(R.id.rvFilters);
        btnCropRotate = findViewById(R.id.btnCropRotate);
        btnFilter = findViewById(R.id.btnFilter);
        btnSave = findViewById(R.id.btnSave);
        btnBack = findViewById(R.id.btnBack);

        originalPath = getIntent().getStringExtra("path");
        if (originalPath != null) {
            sourceUri = Uri.fromFile(new File(originalPath));
            currentUri = sourceUri;
            imgPreview.setImageURI(currentUri);
            setupFilterList();
        }

        btnBack.setOnClickListener(v -> finish());

        // Nút Cắt/Xoay
        btnCropRotate.setOnClickListener(v -> startCrop(currentUri));

        //  Nút Filter
        btnFilter.setOnClickListener(v -> {
            if (rvFilters.getVisibility() == View.VISIBLE) {
                rvFilters.setVisibility(View.GONE);
            } else {
                rvFilters.setVisibility(View.VISIBLE);
            }
        });

        // Nút Lưu
        btnSave.setOnClickListener(v -> {
            if (currentUri != sourceUri || currentFilterType != 0) {
                saveFinalImage();
            } else {
                finishWithResult(originalPath);
            }
        });
    }

    private void setupFilterList() {
        // Tạo dữ liệu filter
        List<FilterAdapter.FilterItem> filters = new ArrayList<>();
        filters.add(new FilterAdapter.FilterItem("Gốc", 0));
        filters.add(new FilterAdapter.FilterItem("Đen Trắng", 1));
        filters.add(new FilterAdapter.FilterItem("Cổ Điển", 2));
        filters.add(new FilterAdapter.FilterItem("Ấm Áp", 3));
        filters.add(new FilterAdapter.FilterItem("Lạnh", 4));

        Bitmap thumb = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
        Bitmap scaledThumb = Bitmap.createScaledBitmap(thumb, 150, 150, false);

        FilterAdapter adapter = new FilterAdapter(filters, scaledThumb, item -> {
            currentFilterType = item.type;

            ColorMatrixColorFilter filter = FilterAdapter.getFilterByType(currentFilterType);
            imgPreview.setColorFilter(filter);

            // rvFilters.setVisibility(View.GONE);
        });

        rvFilters.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvFilters.setAdapter(adapter);
    }

    private void saveFinalImage() {
        try {
            Bitmap originalBitmap = ((BitmapDrawable) imgPreview.getDrawable()).getBitmap();
            Bitmap finalBitmap = Bitmap.createBitmap(originalBitmap.getWidth(), originalBitmap.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(finalBitmap);
            Paint paint = new Paint();

            ColorMatrixColorFilter filter = FilterAdapter.getFilterByType(currentFilterType);
            if (filter != null) paint.setColorFilter(filter);

            canvas.drawBitmap(originalBitmap, 0, 0, paint);

            File file = new File(originalPath);
            FileOutputStream out = new FileOutputStream(file);
            finalBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush(); out.close();

            file.setLastModified(System.currentTimeMillis());
            Toast.makeText(this, "Đã lưu!", Toast.LENGTH_SHORT).show();
            finishWithResult(originalPath);

        } catch (Exception e) { e.printStackTrace(); }
    }

    private void finishWithResult(String path) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("final_path", path);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void startCrop(Uri uri) {
        String destFileName = "cropped_" + System.currentTimeMillis() + ".jpg";
        Uri destUri = Uri.fromFile(new File(getCacheDir(), destFileName));
        UCrop.of(uri, destUri).withAspectRatio(1, 1).withMaxResultSize(1000, 1000).start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP) {
            Uri resultUri = UCrop.getOutput(data);
            if (resultUri != null) {
                currentUri = resultUri;
                imgPreview.setImageURI(null);
                imgPreview.setImageURI(currentUri);

                setupFilterList();

                imgPreview.setColorFilter(FilterAdapter.getFilterByType(currentFilterType));
            }
        }
    }
}