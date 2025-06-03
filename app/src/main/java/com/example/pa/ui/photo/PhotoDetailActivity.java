package com.example.pa.ui.photo;

import static androidx.databinding.DataBindingUtil.setContentView;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.pa.R;
import com.example.pa.ui.post.PostCreateActivity;
import com.example.pa.util.UriToPathHelper;
import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.cloudRepository.GroupRepository;
import com.example.pa.data.model.UploadResponse;
import com.example.pa.data.MockDataManager;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.util.List;
import java.util.ArrayList;

// 点击图片之后能够看到的视图
public class PhotoDetailActivity extends AppCompatActivity {
    private ImageView ivDetail; // 整个大图的背景板和格式
    private ImageButton btnBack; // 左上角返回键
    private LinearLayout toolbar; // 底部工具栏
    private boolean isToolbarVisible = true; // 工具栏和返回键是否可见
    private Button btn_edit;
    private PhotoViewModel photoViewModel;

    // 验证 Uri 有效性
    private boolean isUriValid(Uri uri) {
        try {
            ContentResolver resolver = getContentResolver();
            InputStream stream = resolver.openInputStream(uri);
            if (stream != null) {
                stream.close();
                return true;
            }
            return false;
        } catch (IOException e) {
            Log.e("UriCheck", "URI验证失败: " + e.getMessage());
            return false;
        }
    }

    // 点击图片之后展现大图
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_detail);

        // 初始化 ViewModel
        photoViewModel = new ViewModelProvider(this).get(PhotoViewModel.class);
        photoViewModel.initPhotoDao(this);

        // 全屏设置
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // 以下内容存储在 res.layout.photo_detail_activity.xml里
        ivDetail = findViewById(R.id.iv_detail); // 整个大图的背景板和格式
        btnBack = findViewById(R.id.btn_back); // 左上角返回键
        toolbar = findViewById(R.id.toolbar); // 底部工具栏

//        String imagePath = getIntent().getStringExtra("image_path");
//        Glide.with(this)
//                .load(imagePath)
//                .into(ivDetail);


        // 正确获取 Uri 对象
        Uri imageUri = getIntent().getParcelableExtra("Uri");
        String imagePath = UriToPathHelper.getPathFromUri(getApplicationContext(), imageUri);

        // 添加 Uri 有效性检查
        if (imageUri != null && isUriValid(imageUri)) {
            Glide.with(this)
                    .load(imageUri)
                    .error(R.drawable.error_image) // 添加错误占位图
                    .into(ivDetail);
        } else {
            Toast.makeText(this, "图片加载失败", Toast.LENGTH_SHORT).show();
            finish(); // 关闭当前 Activity
        }

        Button btnEdit = findViewById(R.id.btn_edit);
        btnEdit.setOnClickListener(v -> {
            // 创建跳转意图
            Intent intent = new Intent(PhotoDetailActivity.this, PhotoEditActivity.class);
            intent.putExtra("Uri", imageUri);
            startActivity(intent);

            // 添加过渡动画
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        });

        //TODO: 新加delete按钮
        Button btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(v -> {
            // 获取当前图片路径
            //String imagePath = getIntent().getStringExtra("image_path");
            showDeleteConfirmDialog(imagePath);
        });

        //TODO: 实现share按钮
        Button btnShare = findViewById(R.id.btn_share);
        btnShare.setOnClickListener(v -> {
            // 创建模拟群组数据
            List<GroupInfo> mockGroups = new ArrayList<>();
            GroupInfo mockGroup = new GroupInfo();
            mockGroup.setId("1");
            mockGroup.setName("测试群组");
            mockGroup.setDescription("这是一个测试群组");
            mockGroups.add(mockGroup);

            // 创建群组选择对话框
            String[] groupNames = mockGroups.stream()
                    .map(GroupInfo::getName)
                    .toArray(String[]::new);

            new AlertDialog.Builder(PhotoDetailActivity.this)
                    .setTitle("选择要分享到的群组")
                    .setItems(groupNames, (dialog, which) -> {
                        GroupInfo selectedGroup = mockGroups.get(which);
                        // 将图片URL添加到模拟数据中
                        String imageUrl = imageUri.toString();
                        MockDataManager.getInstance().addMockPost(imageUrl);
                        
                        Toast.makeText(PhotoDetailActivity.this, 
                            "照片已成功分享到群组: " + selectedGroup.getName(), 
                            Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("取消", null)
                    .show();

            // 注释掉实际的API调用，等后端准备好后再启用
            /*
            // 创建 GroupRepository 实例
            GroupRepository groupRepository = new GroupRepository();
            
            // 获取用户已加入的群组
            groupRepository.getJoinedGroups(new GroupRepository.GroupCallback<List<GroupInfo>>() {
                @Override
                public void onSuccess(List<GroupInfo> groups) {
                    if (groups.isEmpty()) {
                        Toast.makeText(PhotoDetailActivity.this, "您还没有加入任何群组", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 创建群组选择对话框
                    String[] groupNames = groups.stream()
                            .map(GroupInfo::getName)
                            .toArray(String[]::new);

                    new AlertDialog.Builder(PhotoDetailActivity.this)
                            .setTitle("选择要分享到的群组")
                            .setItems(groupNames, (dialog, which) -> {
                                GroupInfo selectedGroup = groups.get(which);
                                // 上传照片到选中的群组
                                groupRepository.uploadGroupPhoto(
                                    selectedGroup.getId(),
                                    imageUri,
                                    PhotoDetailActivity.this,
                                    new GroupRepository.GroupCallback<UploadResponse>() {
                                        @Override
                                        public void onSuccess(UploadResponse response) {
                                            Toast.makeText(PhotoDetailActivity.this, 
                                                "照片已成功分享到群组: " + selectedGroup.getName(), 
                                                Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(String errorMessage) {
                                            Toast.makeText(PhotoDetailActivity.this, 
                                                "分享失败: " + errorMessage, 
                                                Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                );
                            })
                            .setNegativeButton("取消", null)
                            .show();
                }

                @Override
                public void onError(String errorMessage) {
                    Toast.makeText(PhotoDetailActivity.this, 
                        "获取群组列表失败: " + errorMessage, 
                        Toast.LENGTH_SHORT).show();
                }
            });
            */
        });


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


    private void showDeleteConfirmDialog(String imagePath) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("the operation cannot be undone, are you sure you want to delete this photo?")
                .setPositiveButton("delete", (dialog, which) -> {
                    // 从文件路径获取文件名
                    String filename = new File(imagePath).getName();

                    // 调用 ViewModel 的删除方法
                    Log.d("DDDDDDelete", filename);
                    photoViewModel.deletePhoto(filename);

                    // 设置结果并关闭页面
                    setResult(RESULT_OK);
                    finish();

                    // 显示删除成功提示
                    Toast.makeText(this, "deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("cancel", null)
                .show();
    }
}
