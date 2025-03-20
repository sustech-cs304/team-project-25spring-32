package com.example.pa.ui.album;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.pa.databinding.FragmentAlbumBinding;

public class AlbumFragment extends Fragment {

    private FragmentAlbumBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        AlbumViewModel albumViewModel =
                new ViewModelProvider(this).get(AlbumViewModel.class);

        binding = FragmentAlbumBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        final TextView textView = binding.textAlbum;
        albumViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}