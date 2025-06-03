package com.example.pa.data;

import android.content.Context;
import android.net.Uri;

import com.example.pa.data.model.post.Post;
import com.example.pa.data.model.UploadResponse;
import com.example.pa.data.cloudRepository.PhotoRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MockDataManager {
    private static MockDataManager instance;
    private Map<String, List<Post>> groupPosts; // 群组ID -> 帖子列表
    private PhotoRepository photoRepository;

    private MockDataManager() {
        groupPosts = new HashMap<>();
        photoRepository = new PhotoRepository();
    }

    public static MockDataManager getInstance() {
        if (instance == null) {
            instance = new MockDataManager();
        }
        return instance;
    }

    public List<Post> getMockPosts(String groupId) {
        return groupPosts.getOrDefault(groupId, new ArrayList<>());
    }

    public void addMockPost(Uri imageUri, String groupId, Context context, final OnPostAddedListener listener) {
        // 使用PhotoRepository上传照片
        photoRepository.uploadPhoto(imageUri, context, new PhotoRepository.PhotoCallback<UploadResponse>() {
            @Override
            public void onSuccess(UploadResponse response) {
                // 上传成功后，创建新的帖子
                int newId = getNextPostId(groupId);
                Post newPost = new Post(newId, new String[]{response.getPath()}, "2024-03-20");
                
                // 将帖子添加到对应群组
                if (!groupPosts.containsKey(groupId)) {
                    groupPosts.put(groupId, new ArrayList<>());
                }
                groupPosts.get(groupId).add(0, newPost);
                
                if (listener != null) {
                    listener.onPostAdded(newPost);
                }
            }

            @Override
            public void onError(String errorMessage) {
                if (listener != null) {
                    listener.onError(errorMessage);
                }
            }
        });
    }

    private int getNextPostId(String groupId) {
        List<Post> posts = groupPosts.getOrDefault(groupId, new ArrayList<>());
        return posts.size() + 1;
    }

    public interface OnPostAddedListener {
        void onPostAdded(Post post);
        void onError(String errorMessage);
    }
} 