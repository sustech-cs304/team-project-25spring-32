package com.example.pa.ui.album;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pa.R;

import java.util.List;
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.ImageViewHolder> {

    private List<String> imageList;

    public AlbumAdapter(List<String> imageList) {
        this.imageList = imageList;
    }

    @Override
    public ImageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 加载每个项的布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ImageViewHolder holder, int position) {
        // 在这里加载图片
        String imagePath = imageList.get(position);
        holder.textView.setText(imagePath);
        // 你可以使用 Glide 或 Picasso 来加载图片
        Glide.with(holder.itemView.getContext())
                .load(imagePath) // 如果是本地图片，路径可以直接使用
                .into(holder.imageView);
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public ImageViewHolder(View itemView) {
            super(itemView);
            // 引用布局中的 ImageView 和 TextView
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.image_text);
        }
    }
}


