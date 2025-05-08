package com.example.pa.ui.album;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pa.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoSelectAdapter extends RecyclerView.Adapter<PhotoSelectAdapter.PhotoViewHolder> {
    private List<Uri> photos = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(Uri uri);
    }

    public PhotoSelectAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Uri> newPhotos) {
        photos = newPhotos;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo_selection, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri uri = photos.get(position);
        holder.bind(uri);
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPhoto;
        private final ImageView ivCheck;
        private final View overlay;

        PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivCheck = itemView.findViewById(R.id.iv_checkbox);
            overlay = itemView.findViewById(R.id.overlay);
        }

        void bind(Uri uri) {
            // 加载图片
            Glide.with(itemView)
                    .load(uri)
                    .thumbnail(0.1f)
                    .centerCrop()
                    .into(ivPhoto);

            // 设置点击监听
            itemView.setOnClickListener(v -> listener.onItemClick(uri));
        }

        void updateSelection(boolean isSelected) {
            ivCheck.setSelected(isSelected);
            overlay.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }
    }
}