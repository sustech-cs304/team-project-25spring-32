package com.example.pa.ui.memory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pa.databinding.FragmentMemoryBinding;

public class MemoryFragment extends Fragment {
    private FragmentMemoryBinding binding;
    private MemoryViewModel memoryViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMemoryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        memoryViewModel = new ViewModelProvider(this).get(MemoryViewModel.class);

        memoryViewModel.getMemoryData().observe(getViewLifecycleOwner(), memoryData -> {
            binding.textMemory.setText(memoryData.title);
            // You could also add a progress bar for memory usage visualization
            // binding.memoryProgressBar.setProgress(memoryData.usagePercentage);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}