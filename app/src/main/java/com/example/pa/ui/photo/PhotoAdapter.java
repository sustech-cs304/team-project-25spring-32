package com.example.pa.ui.photo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pa.R;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    private static List<ImageItem> imageList;

    public PhotoAdapter(List<ImageItem> imageList) {
        PhotoAdapter.imageList = imageList;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_photo, parent, false);
        return new PhotoViewHolder(view);
    }

    // 加载显示图片
    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        ImageItem imageItem = imageList.get(position);

        // 使用Glide加载图片
        Glide.with(holder.itemView.getContext())
                .load(imageItem.getUrl())                  // 加载URL或本地路径
                .placeholder(R.drawable.placeholder_image) // 加载中的占位图
                .error(R.drawable.error_image)             // 加载失败的显示
                .centerCrop()                              // 图片裁剪方式，存放到正方形格子里采用中心裁剪
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public PhotoViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);

            // 从Photo视图跳转到图片大图的具体实现
            itemView.setOnClickListener(v -> {
                Context context = v.getContext();
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    ImageItem item = imageList.get(position);
                    // 下一步跳转至 PhotoDetailActivity
                    Intent intent = new Intent(context, PhotoDetailActivity.class);
                    intent.putExtra("image_url", item.getUrl());
                    context.startActivity(intent);

                    // 添加Activity过渡动画
                    ((Activity) context).overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                } else {
                    Toast.makeText(context, "图片链接无效", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }


    // 更新显示图片列表的方法，将 newList 里的图片展示在photo视图下
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<ImageItem> newList) {
        // 创建新集合避免数据引用问题
        List<ImageItem> tempList = new ArrayList<>(newList);
        imageList.clear();
        imageList.addAll(tempList);
        notifyDataSetChanged(); // 必须确保在主线程调用
    }

}
