package com.example.pa.ui.photo;

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
import com.example.pa.data.Daos.PhotoDao.Photo;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    // 内部持有图片数据列表
    private List<Uri> UriList;
    // 定义点击回调接口
    private OnPhotoClickListener listener;

    // 回调接口：由外部（比如Fragment）实现点击后的操作
    public interface OnPhotoClickListener {
        void onPhotoClick(Uri uri);
    }

    public PhotoAdapter(List<Uri> uriList, OnPhotoClickListener listener) {
        // 创建新集合以避免外部数据引用问题
        this.UriList = new ArrayList<>(uriList);
        this.listener = listener;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Uri uri = UriList.get(position);
        // 使用 Glide 加载图片
        Glide.with(holder.itemView.getContext())
                .load(uri)
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.error_image)
                .centerCrop()
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return UriList.size();
    }

    // 更新数据方法：外部可以调用此方法来刷新图片列表
    public void updateData(List<Uri> uriList) {
        this.UriList.clear();
        this.UriList.addAll(uriList);
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
                    listener.onPhotoClick(UriList.get(position));
                }
            });
        }
    }
}

