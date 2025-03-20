package com.example.pa.ui.photo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pa.databinding.FragmentPhotoBinding;

public class PhotoFragment extends Fragment {

    private FragmentPhotoBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        PhotoViewModel photoViewModel =
                new ViewModelProvider(this).get(PhotoViewModel.class);

        binding = FragmentPhotoBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textPhoto;
        photoViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}