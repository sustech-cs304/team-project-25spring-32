package com.example.pa.ui.memory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.pa.R;
import com.example.pa.data.Daos.MemoryVideoDao.MemoryVideo;
import java.util.List;

public class MemoryAdapter extends RecyclerView.Adapter<MemoryAdapter.MemoryViewHolder> {

    private final List<MemoryVideo> memoryList;
    private final OnMemoryClickListener listener;

    public interface OnMemoryClickListener {
        void onMemoryClick(int memoryId); // 根据实际类型调整
    }

    public MemoryAdapter(List<MemoryVideo> memoryList, OnMemoryClickListener listener) {
        this.memoryList = memoryList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MemoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_memory_card, parent, false);
        return new MemoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MemoryViewHolder holder, int position) {
        MemoryVideo memory = memoryList.get(position);
        holder.textTheme.setText(memory.theme);
        holder.textAlbumName.setText(memory.name);
        holder.textAlbumDate.setText(memory.createdTime);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMemoryClick(memory.id);
            }
        });
    }

    @Override
    public int getItemCount() {
        return memoryList.size();
    }

    static class MemoryViewHolder extends RecyclerView.ViewHolder {
        TextView textTheme;
        ImageView imageCover;
        TextView textAlbumName;
        TextView textAlbumDate;

        public MemoryViewHolder(@NonNull View itemView) {
            super(itemView);
            textTheme = itemView.findViewById(R.id.text_theme);
            imageCover = itemView.findViewById(R.id.image_cover);
            textAlbumName = itemView.findViewById(R.id.text_album_name);
            textAlbumDate = itemView.findViewById(R.id.text_album_date);
        }
    }
}