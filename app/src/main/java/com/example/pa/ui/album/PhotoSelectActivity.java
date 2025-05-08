package com.example.pa.ui.album;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;

import java.util.ArrayList;

public class PhotoSelectActivity extends AppCompatActivity {
    private PhotoSelectViewModel viewModel;
    private PhotoSelectAdapter adapter;
    private ImageButton backButton;
    private TextView btnDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_select);

        // 初始化ViewModel
        viewModel = new ViewModelProvider(this).get(PhotoSelectViewModel.class);

        btnDone = findViewById(R.id.btn_done);
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        // 获取参数
        String albumName = getIntent().getStringExtra("album_name");

        // 初始化视图
        setupViews();
        setupObservers();

        // 加载数据
//        viewModel.loadPhotos(albumName);
        viewModel.loadPhotos("所有照片");
    }

    private void setupViews() {
        // 初始化RecyclerView
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        adapter = new PhotoSelectAdapter(uri -> viewModel.toggleSelection(uri));
        recyclerView.setAdapter(adapter);

        // 设置完成按钮
        findViewById(R.id.btn_done).setOnClickListener(v -> returnResult());
    }

    private void setupObservers() {
        // 观察照片列表
        viewModel.getPhotos().observe(this, photos -> {
            adapter.submitList(photos);
            findViewById(R.id.tv_empty).setVisibility(photos.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // 观察选中数量
        viewModel.getSelectedCount().observe(this, count -> {
            TextView tvCount = findViewById(R.id.tv_selected_count);
            tvCount.setText(String.valueOf(count));

            btnDone.setEnabled(count > 0);
            btnDone.setAlpha(count > 0 ? 1f : 0.5f);
        });
    }

    private void returnResult() {
        Intent result = new Intent();
        result.putParcelableArrayListExtra("selected_photos",
                new ArrayList<>(viewModel.getSelectedUris()));
        setResult(RESULT_OK, result);
        finish();
    }
}