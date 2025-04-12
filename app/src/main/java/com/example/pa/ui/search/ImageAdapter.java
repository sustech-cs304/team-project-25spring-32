package com.example.pa.ui.search;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pa.R;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {

    private List<String> imagePaths;
    private OnImageClickListener listener;

    // 点击事件接口
    public interface OnImageClickListener {
        void onImageClick(String imagePath);
    }

    // 提供给外部设置点击监听器的方法
    public void setOnImageClickListener(OnImageClickListener listener) {
        this.listener = listener;
    }

    public ImageAdapter(List<String> imagePaths) {
        this.imagePaths = imagePaths;
    }

    public void updateImages(List<String> newImagePaths) {
        this.imagePaths = newImagePaths;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);
        Glide.with(holder.itemView.getContext())
                .load(imagePath)
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    // 内部 ViewHolder 类，实现点击事件
     class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.imageView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onImageClick(imagePaths.get(position));
                }
            });
        }
    }
}