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
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<String> albumList;
    private OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(String albumName);
    }

    public AlbumAdapter(List<String> albumList, OnAlbumClickListener listener) {
        this.albumList = albumList;
        this.listener = listener;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 加载每个项的布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AlbumViewHolder holder, int position) {
        // 在这里加载图片
        String albumName = albumList.get(position);
        holder.textView.setText(albumName);
        // 你可以使用 Glide 或 Picasso 来加载图片
        Glide.with(holder.itemView.getContext())
                .load(albumName) // 如果是本地图片，路径可以直接使用
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlbumClick(albumName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            // 引用布局中的 ImageView 和 TextView
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.image_text);
        }
    }
}


