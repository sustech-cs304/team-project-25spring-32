package com.example.pa.ui.photo;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pa.R;

// 点击图片之后能够看到的视图
public class PhotoDetailActivity extends AppCompatActivity {
    private ImageView ivDetail; // 整个大图的背景板和格式
    private ImageButton btnBack; // 左上角返回键
    private LinearLayout toolbar; // 底部工具栏
    private boolean isToolbarVisible = true; // 工具栏和返回键是否可见
    private Button btn_edit;



    // 点击图片之后展现大图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.photo_detail_activity);

        // 全屏设置
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // 以下内容存储在 res.latout.photo_detail_activity.xml里
        ivDetail = findViewById(R.id.iv_detail); // 整个大图的背景板和格式
        btnBack = findViewById(R.id.btn_back); // 左上角返回键
        toolbar = findViewById(R.id.toolbar); // 底部工具栏
        //TODO: 目前有工具栏有 edit share两个按钮，点击功能暂未实现
        // 获取编辑按钮并设置点击事件
        Button btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(v -> {
            // 获取当前图片URL
            String imageUrl = getIntent().getStringExtra("image_url");

            // 创建跳转意图
            Intent intent = new Intent(PhotoDetailActivity.this, PhotoEditActivity.class);
            intent.putExtra("image_url", imageUrl);
            startActivity(intent);

            // 添加过渡动画（可选）
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        // 加载图片，目前支持URL导入图片
        // TODO: 希望能够通过本地存储导入图片，希望对URL图片进行缓存加速
        String imageUrl = getIntent().getStringExtra("image_url");
        Glide.with(this)
                .load(imageUrl)
                .into(ivDetail);

        // 点击图片切换工具栏可见性
        ivDetail.setOnClickListener(v -> toggleToolbar());

        // 返回按钮点击事件
        btnBack.setOnClickListener(v -> finish());

        // 初始显示工具栏
        showToolbar(true);
    }

    private void toggleToolbar() {
        isToolbarVisible = !isToolbarVisible;
        showToolbar(isToolbarVisible);
    }

    private void showToolbar(boolean show) {
        float toolbarAlpha = show ? 1.0f : 0.0f;
        float btnBackAlpha = show ? 1.0f : 0.0f;

        toolbar.animate()
                .alpha(toolbarAlpha) // 设置目标透明度（0.0 完全透明 ~ 1.0 完全不透明）
                .setDuration(300)
                .withStartAction(() -> toolbar.setVisibility(View.VISIBLE))
                .withEndAction(() -> {
                    if (!show) toolbar.setVisibility(View.GONE);
                });

        btnBack.animate()
                .alpha(btnBackAlpha)
                .setDuration(300)
                .withStartAction(() -> btnBack.setVisibility(View.VISIBLE))
                .withEndAction(() -> {
                    if (!show) btnBack.setVisibility(View.GONE);
                });
    }
}
