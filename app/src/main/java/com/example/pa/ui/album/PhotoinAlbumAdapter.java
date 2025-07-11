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

import com.example.pa.data.model.Photo;
import com.example.pa.data.FileRepository;

public class PhotoinAlbumAdapter extends RecyclerView.Adapter<PhotoinAlbumAdapter.PhotoViewHolder> {

    private List<Uri> imageList;

    // 定义点击回调接口
    private OnPhotoClickListener listener;
    // 回调接口：由外部（比如Fragment）实现点击后的操作
    public interface OnPhotoClickListener {
        void onPhotoClick(Uri imageItem);
    }

    public PhotoinAlbumAdapter(List<Uri> imageList, OnPhotoClickListener listener) {
        // 创建新集合以避免外部数据引用问题
        this.imageList = new ArrayList<>(imageList);
        this.listener = listener;

    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri imageItem = imageList.get(position);
        // 使用 Glide 加载图片
        Glide.with(holder.itemView.getContext())
                .load(imageItem)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public void updateData(List<Uri> newList) {
        this.imageList.clear();
        this.imageList.addAll(newList);
        notifyDataSetChanged(); // 确保在主线程调用
    }

    // 内部的 ViewHolder 实现点击事件，通过接口回调通知外部
    class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onPhotoClick(imageList.get(position));
                }
            });
        }
    }
}
