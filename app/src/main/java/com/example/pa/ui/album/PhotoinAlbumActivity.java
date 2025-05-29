package com.example.pa.ui.album;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pa.R;
import com.example.pa.ui.select.PhotoSelectActivity;

import java.util.ArrayList;

public class PhotoinAlbumActivity extends AppCompatActivity {
    private ImageButton backButton;
    private ImageView addIcon;
    private ImageView moreIcon;
    private static final int REQUEST_SELECT_PHOTOS = 1001;
    private String currentAlbumName;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_in_album);

        // 获取传递的相册名称"
        currentAlbumName = getIntent().getStringExtra("album_name");

        // 加载PhotoinAlbumFragment到容器
        if (savedInstanceState == null) {
            PhotoinAlbumFragment fragment = PhotoinAlbumFragment.newInstance(currentAlbumName);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.photo_fragment_container, fragment)
                    .addToBackStack(null)
                    .commit();
        }

        // 设置返回按钮的点击事件
        // 用于处理返回按钮
        backButton = findViewById(R.id.back_button);
        backButton.setOnClickListener(v -> finish());

        addIcon = findViewById(R.id.add_icon_in_album);
        addIcon.setOnClickListener(v -> onAddClick());

        moreIcon = findViewById(R.id.more_icon_in_album);
        moreIcon.setOnClickListener(v -> onMoreClick());

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 检查请求码和结果码
        if (requestCode == REQUEST_SELECT_PHOTOS && resultCode == RESULT_OK) {
            if (data != null) {
                // 获取返回的选中图片 URI 列表
                ArrayList<Uri> selectedUris = data.getParcelableArrayListExtra("selected_photos");

                // 处理选中的图片
                if (requestCode == REQUEST_SELECT_PHOTOS && resultCode == RESULT_OK && data != null) {
                    // 获取返回的照片列表和操作类型
                    ArrayList<Uri> selectedPhotos = data.getParcelableArrayListExtra("selected_photos");
                    String operationType = data.getStringExtra("operation_type");

                    // 找到当前显示的Fragment
                    Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.photo_fragment_container);
                    if (fragment instanceof PhotoinAlbumFragment) {
                        PhotoinAlbumFragment photoFragment = (PhotoinAlbumFragment) fragment;

                        // 将结果传递给Fragment处理
                        photoFragment.handlePhotoSelectionResult(selectedPhotos, operationType);
                    }
                }
            }
        }
    }

    public void onAddClick() {
        Intent intent = new Intent(this, PhotoSelectActivity.class);
        startActivityForResult(intent, REQUEST_SELECT_PHOTOS);
    }

    public void onMoreClick() {

    }
}