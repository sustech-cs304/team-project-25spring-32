package com.example.pa.ui.ai;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pa.databinding.FragmentAiBinding;

public class AIFragment extends Fragment {

    private FragmentAiBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AIViewModel AIViewModel =
                new ViewModelProvider(this).get(AIViewModel.class);

        binding = FragmentAiBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textAi;
        AIViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}