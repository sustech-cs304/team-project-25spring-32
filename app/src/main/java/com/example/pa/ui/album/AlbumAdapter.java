package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.net.Uri;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.example.pa.MyApplication;
import com.example.pa.R;
import com.example.pa.data.Daos.AlbumDao.Album;
import com.example.pa.data.FileRepository;

import java.util.List;

/**
 * AI-generated-content
 * tool: ChatGPT
 * version: 4o
 * usage: I described my UI design to it, and asked how to program.
 * I use the generated code as template.
 */
public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private List<Album> albumList;
    private OnAlbumClickListener listener;
    private final FileRepository fileRepository;
    private final SparseArray<Long> positionTimestamps = new SparseArray<>();
    private boolean isManageMode = false;  // 控制删除图标显示
    private AlbumFragment.AlbumType albumType;

    public interface OnAlbumClickListener {
        void onAlbumClick(String albumName);
        void onDeleteAlbum(Album album);
    }

    public AlbumAdapter(List<Album> albumList, OnAlbumClickListener listener, AlbumFragment.AlbumType albumType) {
        this.albumList = albumList;
        this.listener = listener;
        this.fileRepository = MyApplication.getInstance().getFileRepository();
        this.albumType = albumType;
    }

    @Override
    public AlbumViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 加载每个项的布局
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    /**
     * AI-generated-content
     * tool: DeepSeek
     * version: R1
     * usage: I asked how to solve the problem of asynchronous scan, and
     * modify the code according to my existed code.
     */
    @Override
    public void onBindViewHolder(AlbumViewHolder holder, @SuppressLint("RecyclerView") int position) {
        final long timestamp = System.currentTimeMillis();
        positionTimestamps.put(position, timestamp);

        // 在这里加载图片
        Album album = albumList.get(position);
        holder.textView.setText(album.name);
        Log.d("AlbumAdapter", "onBindViewHolder: " + album.name);

//        // 根据相册类型设置样式
//        switch (albumType) {
//            case CUSTOM:
//                holder.typeBadge.setVisibility(View.VISIBLE);
//                holder.typeBadge.setText("自定义");
//                holder.typeBadge.setBackgroundResource(R.drawable.bg_custom_badge);
//                break;
//            case TIME:
//                holder.typeBadge.setVisibility(View.VISIBLE);
//                holder.typeBadge.setText("时间");
//                holder.typeBadge.setBackgroundResource(R.drawable.bg_time_badge);
//                break;
//            case LOCATION:
//                holder.typeBadge.setVisibility(View.VISIBLE);
//                holder.typeBadge.setText("地点");
//                holder.typeBadge.setBackgroundResource(R.drawable.bg_location_badge);
//                break;
//        }

        // 你可以使用 Glide 或 Picasso 来加载图片
        Glide.with(holder.itemView.getContext())
                .load(getAlbumCover(album))
                .into(holder.imageView);

        // 带回调的扫描
        fileRepository.triggerMediaScanForAlbum(album.name, new FileRepository.MediaScanCallback() {
            @Override
            public void onScanCompleted(Uri uri) {
                // 验证视图是否仍然有效
                if (positionTimestamps.get(position, -1L) != timestamp) return;

                Uri coverUri = getAlbumCover(album);
                if (coverUri != null) {
                    Glide.with(holder.itemView.getContext())
                            .load(coverUri)
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(holder.imageView);
                }
            }

            @Override
            public void onScanFailed(String error) {
                Log.e("AlbumAdapter", error);
            }
        });

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

    private Uri getAlbumCover(Album album) {
        return album.cover != null ? Uri.parse(album.cover) : fileRepository.getAlbumCover(album.name);
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

    @SuppressLint("NotifyDataSetChanged")
    public void updateAlbums(List<Album> newAlbums) {
        this.albumList = newAlbums;
        notifyDataSetChanged();
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView textView;
//        TextView typeBadge; // 新增类型标识
        ImageView deleteIcon;

        public AlbumViewHolder(View itemView) {
            super(itemView);
            // 引用布局中的 ImageView 和 TextView
            imageView = itemView.findViewById(R.id.imageView);
            textView = itemView.findViewById(R.id.image_text);
//            typeBadge = itemView.findViewById(R.id.type_badge); // 新增视图
            deleteIcon = itemView.findViewById(R.id.delete_icon);
        }
    }
}


