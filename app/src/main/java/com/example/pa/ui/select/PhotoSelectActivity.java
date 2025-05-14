package com.example.pa.ui.select;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;
import com.example.pa.databinding.ActivityMultiSelectBinding;

import java.util.ArrayList;

public class PhotoSelectActivity extends AppCompatActivity {
    private PhotoSelectViewModel viewModel;
    private PhotoSelectAdapter adapter;
    private ActivityMultiSelectBinding binding; // 使用 View Binding

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMultiSelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(PhotoSelectViewModel.class);

        // 初始化点击监听
        binding.backButton.setOnClickListener(v -> finish());
        binding.btnDone.setOnClickListener(v -> returnResult());

        // 初始化 RecyclerView
        setupRecyclerView();

        // 设置数据观察
        setupObservers();

        // 加载数据（示例使用固定值，实际应从 Intent 获取）
        viewModel.loadPhotos("所有照片");
    }

    private void setupRecyclerView() {
        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        binding.recyclerView.setLayoutManager(layoutManager);

        // 使用改进后的 Adapter
        adapter = new PhotoSelectAdapter(new PhotoSelectAdapter.OnSelectionChangeListener() {
            @Override
            public void onSelectionChanged(int selectedCount) {
                // 直接更新 ViewModel 状态
                viewModel.updateSelectionCount(selectedCount);
            }
        });

        binding.recyclerView.setAdapter(adapter);
//        binding.recyclerView.addItemDecoration(new GridSpacingItemDecoration(3, 4, true));
    }

    private void setupObservers() {
        // 观察照片数据
        viewModel.getPhotos().observe(this, photos -> {
            adapter.submitList(photos);
            binding.textNoPhoto.setVisibility(photos.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // 观察选中状态
        viewModel.getSelectedCount().observe(this, count -> {
            binding.tvSelectedCount.setText(String.valueOf(count));
            binding.textEmpty.setVisibility(count == 0 ? View.VISIBLE : View.GONE);

            // 更新完成按钮状态
            binding.btnDone.setEnabled(count > 0);
            binding.btnDone.animate()
                    .alpha(count > 0 ? 1f : 0.5f)
                    .setDuration(200)
                    .start();
        });
    }

    private void returnResult() {
        Intent result = new Intent();
        if (viewModel.getSelectedCount().getValue() != null && viewModel.getSelectedCount().getValue() > 0) {
            result.putParcelableArrayListExtra("selected_photos",
                    new ArrayList<>(adapter.getSelectedUris()));
        }
        setResult(RESULT_OK, result);
        finish();
    }

//    // 处理配置变更（可选）
//    @Override
//    protected void onSaveInstanceState(@NonNull Bundle outState) {
//        super.onSaveInstanceState(outState);
//        outState.putParcelableArrayList("selected_uris",
//                new ArrayList<>(adapter.getSelectedUris()));
//    }
//
//    @Override
//    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
//        super.onRestoreInstanceState(savedInstanceState);
//        ArrayList<Uri> savedUris = savedInstanceState.getParcelableArrayList("selected_uris");
//        if (savedUris != null) {
//            adapter.restoreSelections(savedUris);
//        }
//    }
}