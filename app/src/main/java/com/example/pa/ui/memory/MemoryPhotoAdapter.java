// MemoryPhotoAdapter.java
package com.example.pa.ui.memory;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.pa.R;
import com.example.pa.ui.album.PhotoinAlbumAdapter;

import java.util.List;

public class MemoryPhotoAdapter extends RecyclerView.Adapter<MemoryPhotoAdapter.ViewHolder> {

    private final List<Uri> photoUris;
    private OnPhotoClickListener listener;
    // 回调接口：实现点击后的操作
    public interface OnPhotoClickListener {
        void onPhotoClick(Uri imageUri);
    }

    public MemoryPhotoAdapter(List<Uri> photoUris, OnPhotoClickListener listener) {
        this.photoUris = photoUris;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_memory_photo, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Uri uri = photoUris.get(position);
        Glide.with(holder.itemView.getContext())
                .load(uri)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return photoUris.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.photo_image);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPhotoClick(photoUris.get(position));
                }
            });
        }
    }
}