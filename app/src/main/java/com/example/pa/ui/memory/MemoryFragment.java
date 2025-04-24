package com.example.pa.ui.memory;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.data.Daos.AlbumDao.Album;
import com.example.pa.ui.album.AlbumAdapter;
import com.example.pa.ui.album.AlbumViewModel;

import java.util.ArrayList;

public class MemoryFragment extends Fragment {

    private RecyclerView recyclerView;
    private MemoryAdapter memoryAdapter;
    private MemoryViewModel memoryViewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_memory, container, false);
        recyclerView = rootView.findViewById(R.id.recyclerView_memory);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        memoryViewModel = new ViewModelProvider(this).get(MemoryViewModel.class);

        memoryViewModel.getMemoryVideos().observe(getViewLifecycleOwner(), memoryVideos -> {
            memoryAdapter = new MemoryAdapter(memoryVideos);
            recyclerView.setAdapter(memoryAdapter);
        });
    }
}
