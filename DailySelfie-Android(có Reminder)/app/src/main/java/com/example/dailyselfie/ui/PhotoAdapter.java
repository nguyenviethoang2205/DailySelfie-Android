package com.example.dailyselfie.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.bumptech.glide.Glide;
import com.example.dailyselfie.R;
import com.example.dailyselfie.model.PhotoItem;

import java.io.File;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PhotoItem> items;
    private OnPhotoClickListener listener;

    public interface OnPhotoClickListener {
        void onPhotoClick(File file);
    }

    public PhotoAdapter(List<PhotoItem> items, OnPhotoClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PhotoItem.TYPE_DATE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_date_header, parent, false);
            return new DateViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_photo, parent, false);
            return new PhotoViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PhotoItem item = items.get(position);

        // Xử lý header ngày
        if (item.type == PhotoItem.TYPE_DATE) {

            StaggeredGridLayoutManager.LayoutParams params =
                    (StaggeredGridLayoutManager.LayoutParams) holder.itemView.getLayoutParams();
            params.setFullSpan(true);
            holder.itemView.setLayoutParams(params);

            ((DateViewHolder) holder).txtDate.setText(item.date);
            return;
        }

        // Xử lý ảnh
        Glide.with(holder.itemView.getContext())
                .load(item.file)
                .into(((PhotoViewHolder) holder).imageView);

        holder.itemView.setOnClickListener(v ->
                listener.onPhotoClick(item.file)
        );
    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    // ViewHolder cho ảnh
    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);
        }
    }

    // ViewHolder cho header ngày
    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        public DateViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }
}

