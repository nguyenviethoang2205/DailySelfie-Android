package com.example.dailyselfie.ui;

import android.graphics.Bitmap;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dailyselfie.R;

import java.util.List;

public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.FilterViewHolder> {

    private List<FilterItem> filterList;
    private OnFilterSelectListener listener;
    private Bitmap thumbnailBitmap;

    public interface OnFilterSelectListener {
        void onFilterSelected(FilterItem filter);
    }

    public static class FilterItem {
        public String name;
        public int type;

        public FilterItem(String name, int type) {
            this.name = name;
            this.type = type;
        }
    }

    public FilterAdapter(List<FilterItem> filterList, Bitmap thumbnailBitmap, OnFilterSelectListener listener) {
        this.filterList = filterList;
        this.thumbnailBitmap = thumbnailBitmap;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FilterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter, parent, false);
        return new FilterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterViewHolder holder, int position) {
        FilterItem item = filterList.get(position);
        holder.txtName.setText(item.name);

        if (thumbnailBitmap != null) {
            holder.imgThumbnail.setImageBitmap(thumbnailBitmap);
        }

        holder.imgThumbnail.setColorFilter(getFilterByType(item.type));

        holder.itemView.setOnClickListener(v -> listener.onFilterSelected(item));
    }

    @Override
    public int getItemCount() {
        return filterList.size();
    }

    static class FilterViewHolder extends RecyclerView.ViewHolder {
        ImageView imgThumbnail;
        TextView txtName;

        public FilterViewHolder(@NonNull View itemView) {
            super(itemView);
            imgThumbnail = itemView.findViewById(R.id.imgFilterThumbnail);
            txtName = itemView.findViewById(R.id.txtFilterName);
        }
    }

    public static ColorMatrixColorFilter getFilterByType(int type) {
        ColorMatrix matrix = new ColorMatrix();
        switch (type) {
            case 1: // BW
                matrix.setSaturation(0);
                break;
            case 2: // Sepia
                matrix.setSaturation(0);
                ColorMatrix sepia = new ColorMatrix();
                sepia.setScale(1f, 0.95f, 0.82f, 1f);
                matrix.postConcat(sepia);
                break;
            case 3: // Warm
                matrix.setScale(1.1f, 1f, 0.9f, 1f);
                break;
            case 4: // Cool
                matrix.setScale(0.9f, 0.9f, 1.1f, 1f);
                break;
            default: // Original
                return null;
        }
        return new ColorMatrixColorFilter(matrix);
    }
}