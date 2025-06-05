package com.example.pa.ui.select;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

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
        binding.btnDone.setOnClickListener(v -> OnDoneClicked());
        binding.btnCopy.setOnClickListener(v -> OnCopyClicked());
        binding.btnMove.setOnClickListener(v -> OnMoveClicked());

        // 初始化 RecyclerView
        setupRecyclerView();

        // 设置数据观察
        setupObservers();

        // 加载数据（示例使用固定值，实际应从 Intent 获取）
        viewModel.loadPhotos("All Photos");
    }

    private void OnDoneClicked() {
        showChoiceLayer();
    }

    private void showChoiceLayer() {
        binding.maskLayer.setVisibility(View.VISIBLE);
        binding.operationPanel.setVisibility(View.VISIBLE);
    }

    private void hideChoiceLayer() {
        binding.maskLayer.setVisibility(View.GONE);
        binding.operationPanel.setVisibility(View.GONE);
    }

    private void OnCopyClicked() {
        returnResult("copy");
    }

    private void OnMoveClicked() {
        returnResult("move");
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

    private void returnResult(String operationType) {
        Intent result = new Intent();
        if (viewModel.getSelectedCount().getValue() != null && viewModel.getSelectedCount().getValue() > 0) {
            result.putParcelableArrayListExtra("selected_photos",
                    new ArrayList<>(adapter.getSelectedUris()));
            result.putExtra("operation_type", operationType);
        }
        setResult(RESULT_OK, result);
        finish();
    }
}