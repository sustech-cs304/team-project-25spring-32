package com.example.pa.ui.memory;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pa.databinding.FragmentAiBinding;

public class MemoryFragment extends Fragment {

    private FragmentAiBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MemoryViewModel MemoryViewModel =
                new ViewModelProvider(this).get(MemoryViewModel.class);

        binding = FragmentAiBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textMemory;
        MemoryViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}