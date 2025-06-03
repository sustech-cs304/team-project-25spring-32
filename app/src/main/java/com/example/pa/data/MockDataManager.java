package com.example.pa.data;

import com.example.pa.data.model.post.Post;
import java.util.ArrayList;
import java.util.List;

public class MockDataManager {
    private static MockDataManager instance;
    private List<Post> mockPosts;

    private MockDataManager() {
        mockPosts = new ArrayList<>();
        // 初始化一些测试数据
        mockPosts.add(new Post(1, new String[]{"https://picsum.photos/200/300"}, "2024-03-20"));
        mockPosts.add(new Post(2, new String[]{"https://picsum.photos/200/301"}, "2024-03-20"));
    }

    public static MockDataManager getInstance() {
        if (instance == null) {
            instance = new MockDataManager();
        }
        return instance;
    }

    public List<Post> getMockPosts() {
        return mockPosts;
    }

    public void addMockPost(String imageUrl) {
        int newId = mockPosts.size() + 1;
        mockPosts.add(0, new Post(newId, new String[]{imageUrl}, "2024-03-20"));
    }
} 