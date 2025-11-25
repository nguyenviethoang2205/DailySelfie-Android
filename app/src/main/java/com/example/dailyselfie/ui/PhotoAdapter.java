package com.example.dailyselfie.ui;

import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.bumptech.glide.Glide;
import com.bumptech.glide.signature.ObjectKey;
import com.example.dailyselfie.R;
import com.example.dailyselfie.model.PhotoItem;

import java.io.File;
import java.util.*;

public class PhotoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<PhotoItem> items;
    private OnItemClick listener;

    public Set<String> selectedPhotos = new HashSet<>();
    public boolean isSelectMode = false;

    public interface OnItemClick {
        void onClick(File file);
        void onLongClick(File file);
        void onAddNote(File file);
    }

    public PhotoAdapter(List<PhotoItem> items, OnItemClick listener) {
        this.items = items;
        this.listener = listener;
    }

    public void startSelectMode(String path) {
        isSelectMode = true;
        selectedPhotos.add(path);
        notifyDataSetChanged();
    }

    public void toggleSelect(String path) {
        if (selectedPhotos.contains(path)) {
            selectedPhotos.remove(path);
        } else {
            selectedPhotos.add(path);
        }
        notifyDataSetChanged();
    }

    public void exitSelectMode() {
        isSelectMode = false;
        selectedPhotos.clear();
        notifyDataSetChanged();
    }

    public int getSelectedCount() {
        return selectedPhotos.size();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position).type;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == PhotoItem.TYPE_DATE) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_date_header, parent, false);
            return new DateViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_photo, parent, false);
            return new PhotoHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PhotoItem item = items.get(position);
        if (item.type == PhotoItem.TYPE_DATE) {
            ((DateViewHolder) holder).txtDate.setText(item.date);
            return;
        }

        PhotoHolder h = (PhotoHolder) holder;
        String path = item.file.getAbsolutePath();
        boolean isSelected = selectedPhotos.contains(path);

        Glide.with(h.itemView.getContext())
                .load(item.file)
                .signature(new ObjectKey(item.file.lastModified()))
                .into(h.img);

        // Hiển thị dấu tick khi chọn
        h.imgSelected.setVisibility(isSelected ? View.VISIBLE : View.GONE);

        // Hiển thị ghi chú
        String note = MainActivity.photoNotes.get(path);
        if (note != null && !note.isEmpty()) {
            h.tvNote.setText(note);
            h.tvNote.setVisibility(View.VISIBLE);
        } else {
            h.tvNote.setVisibility(View.GONE);
        }

        h.itemView.setOnClickListener(v -> listener.onClick(item.file));
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(item.file);
            return true;
        });

        h.btnAddNote.setOnClickListener(v -> listener.onAddNote(item.file));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    class PhotoHolder extends RecyclerView.ViewHolder {
        ImageView img, imgSelected;
        TextView tvNote;
        ImageButton btnAddNote;

        PhotoHolder(View v) {
            super(v);
            img = v.findViewById(R.id.imageView);
            imgSelected = v.findViewById(R.id.imgSelected);
            tvNote = v.findViewById(R.id.tvNote);
            btnAddNote = v.findViewById(R.id.btnAddNote);
        }
    }

    class DateViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate;
        DateViewHolder(View v) {
            super(v);
            txtDate = v.findViewById(R.id.txtDate);
        }
    }
}