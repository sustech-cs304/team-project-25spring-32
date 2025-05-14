package com.example.pa.ui.select;

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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhotoSelectAdapter extends RecyclerView.Adapter<PhotoSelectAdapter.PhotoViewHolder> {
    private List<Uri> photos = new ArrayList<>();
    private final OnSelectionChangeListener listener;
    private final Set<Uri> selectedUris = new HashSet<>(); // 新增选中状态集合

    // 修改接口为选中状态监听
    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
    }

    public PhotoSelectAdapter(OnSelectionChangeListener listener) {
        this.listener = listener;
    }

    public void submitList(List<Uri> newPhotos) {
        photos = new ArrayList<>(newPhotos); // 使用副本保证数据安全
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
        boolean isSelected = selectedUris.contains(uri); // 获取选中状态

        holder.bind(uri, isSelected); // 传递选中状态

        holder.itemView.setOnClickListener(v -> {
            toggleSelection(uri); // 切换选中状态
            notifyItemChanged(position); // 局部刷新

            // 通知外部选中数量变化
            if (listener != null) {
                listener.onSelectionChanged(selectedUris.size());
            }
        });
    }

    // 新增选中状态切换方法
    private void toggleSelection(Uri uri) {
        if (selectedUris.contains(uri)) {
            selectedUris.remove(uri);
        } else {
            selectedUris.add(uri);
        }
    }

    public void restoreSelections(List<Uri> uris) {
        selectedUris.clear();
        selectedUris.addAll(uris);
        notifyDataSetChanged();
        if (listener != null) {
            listener.onSelectionChanged(selectedUris.size());
        }
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    // 新增获取选中项方法
    public List<Uri> getSelectedUris() {
        return new ArrayList<>(selectedUris);
    }

    static class PhotoViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivPhoto;
        private final ImageView ivCheck;
        private final View overlay;

        PhotoViewHolder(View itemView) {
            super(itemView);
            ivPhoto = itemView.findViewById(R.id.iv_photo);
            ivCheck = itemView.findViewById(R.id.iv_checkbox);
            overlay = itemView.findViewById(R.id.overlay);
        }

        void bind(Uri uri, boolean isSelected) {
            // 加载图片
            Glide.with(itemView)
                    .load(uri)
                    .thumbnail(0.1f)
                    .centerCrop()
                    .into(ivPhoto);

            // 更新选中状态显示
            updateSelection(isSelected);
        }

        void updateSelection(boolean isSelected) {
            // 关键修改：通过setSelected控制状态
            ivCheck.setSelected(isSelected);
            overlay.setVisibility(isSelected ? View.VISIBLE : View.INVISIBLE);
        }
    }
}