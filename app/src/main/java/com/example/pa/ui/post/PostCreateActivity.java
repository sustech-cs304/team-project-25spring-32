package com.example.pa.ui.post;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.pa.R;
import com.example.pa.data.Daos.GroupDao;
import com.example.pa.data.Daos.PostDao;
import com.example.pa.data.Daos.UserDao;
import com.example.pa.data.models.Post;

import java.util.ArrayList;
import java.util.List;

public class PostCreateActivity extends AppCompatActivity {
    private static final String TAG = "PostCreateActivity";
    
    private ImageView imagePreview;
    private EditText titleInput;
    private EditText contentInput;
    private Spinner groupSpinner;
    private Button publishButton;
    private Button cancelButton;
    
    private Uri imageUri;
    private PostDao postDao;
    private GroupDao groupDao;
    private UserDao userDao;
    private List<Integer> groupIds = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_create);

        try {
            // 初始化DAO
            postDao = new PostDao(this);
            groupDao = new GroupDao(this);
            userDao = new UserDao(this);

            // 初始化视图
            imagePreview = findViewById(R.id.imagePreview);
            titleInput = findViewById(R.id.titleInput);
            contentInput = findViewById(R.id.contentInput);
            groupSpinner = findViewById(R.id.groupSpinner);
            publishButton = findViewById(R.id.publishButton);
            cancelButton = findViewById(R.id.cancelButton);

            // 获取图片URI
            imageUri = getIntent().getParcelableExtra("Uri");
            if (imageUri == null) {
                Log.e(TAG, "No image URI provided");
                Toast.makeText(this, "无法获取图片", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 加载图片预览
            Glide.with(this)
                    .load(imageUri)
                    .centerCrop()
                    .into(imagePreview);

            // 加载用户所在的群组
            loadUserGroups();

            // 设置按钮点击事件
            publishButton.setOnClickListener(v -> publishPost());
            cancelButton.setOnClickListener(v -> finish());
        } catch (Exception e) {
            Log.e(TAG, "初始化失败", e);
            Toast.makeText(this, "初始化失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void loadUserGroups() {
        try {
            // 获取当前用户ID（这里假设是1，实际应该从登录状态获取）
            int currentUserId = 1;
            
            // 获取用户所在的群组
            var cursor = groupDao.getUserGroups(currentUserId);
            List<String> groupNames = new ArrayList<>();
            groupIds.clear();

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int groupId = cursor.getInt(cursor.getColumnIndexOrThrow(GroupDao.COLUMN_ID));
                    String groupName = cursor.getString(cursor.getColumnIndexOrThrow(GroupDao.COLUMN_NAME));
                    groupIds.add(groupId);
                    groupNames.add(groupName);
                } while (cursor.moveToNext());
                cursor.close();
            }

            if (groupNames.isEmpty()) {
                Toast.makeText(this, "您还没有加入任何群组", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }

            // 设置Spinner适配器
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, groupNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            groupSpinner.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "加载群组失败", e);
            Toast.makeText(this, "加载群组失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void publishPost() {
        try {
            String title = titleInput.getText().toString().trim();
            String content = contentInput.getText().toString().trim();
            int selectedGroupId = groupIds.get(groupSpinner.getSelectedItemPosition());

            if (title.isEmpty()) {
                Toast.makeText(this, "请输入标题", Toast.LENGTH_SHORT).show();
                return;
            }

            if (content.isEmpty()) {
                Toast.makeText(this, "请输入内容", Toast.LENGTH_SHORT).show();
                return;
            }

            // 创建帖子
            Post post = new Post();
            post.setImageUri(imageUri.toString());
            post.setTitle(title);
            post.setContent(content);
            post.setAuthorId(1); // 这里应该使用实际的用户ID
            post.setGroupId(selectedGroupId);

            Log.d(TAG, "Creating post with title: " + title + ", content length: " + content.length());

            long result = postDao.createPost(post);
            if (result != -1) {
                Log.d(TAG, "Post created successfully with ID: " + result);
                Toast.makeText(this, "发布成功", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Log.e(TAG, "Failed to create post");
                Toast.makeText(this, "发布失败", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "发布帖子失败", e);
            Toast.makeText(this, "发布失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
} 