package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pa.R;
import com.example.pa.data.Daos.AlbumDao.Album;

import java.util.List;
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<Album> albumList;
    private OnAlbumClickListener listener;
    private boolean isManageMode = false;  // 控制删除图标显示

    public interface OnAlbumClickListener {
        void onAlbumClick(String albumName);
        void onDeleteAlbum(Album album);
    }

    public AlbumAdapter(List<Album> albumList, OnAlbumClickListener listener) {
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
        Album album = albumList.get(position);
        holder.textView.setText(album.name);
        // 你可以使用 Glide 或 Picasso 来加载图片
        Glide.with(holder.itemView.getContext())
                .load(album) // 如果是本地图片，路径可以直接使用
                .into(holder.imageView);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onAlbumClick(album.name);
            }
        });

        // 绑定删除图标的显示与点击事件
        if (isManageMode) {
            holder.deleteIcon.setVisibility(View.VISIBLE);
            holder.deleteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteAlbum(album);  // 调用删除接口
                }
            });
        } else {
            holder.deleteIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return albumList.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setManageMode(boolean isManageMode) {
        this.isManageMode = isManageMode;
        notifyDataSetChanged();  // 更新图标显示
    }

    public boolean getManageMode(){
        return isManageMode;
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
        ImageView deleteIcon;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            // 引用布局中的 ImageView 和 TextView
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.image_text);
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}


