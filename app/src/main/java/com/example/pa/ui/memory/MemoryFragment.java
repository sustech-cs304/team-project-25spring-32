package com.example.pa.ui.memory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pa.databinding.FragmentMemoryBinding;

public class MemoryFragment extends Fragment {

    private FragmentMemoryBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MemoryViewModel MemoryViewModel =
                new ViewModelProvider(this).get(MemoryViewModel.class);

        binding = FragmentMemoryBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}