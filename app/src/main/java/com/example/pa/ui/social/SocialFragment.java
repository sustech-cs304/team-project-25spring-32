package com.example.pa.ui.social;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;

public class SocialFragment extends Fragment {

    private RecyclerView recyclerView;
    private PostAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private List<Post> postList = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social, container, false);

        // 初始化视图
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);

        // 设置RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new PostAdapter(postList);
        recyclerView.setAdapter(adapter);

        // 加载初始数据
        loadPosts();

        // 设置下拉刷新
        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadPosts();
            swipeRefreshLayout.setRefreshing(false);
        });

        return view;
    }

    private void loadPosts() {
        // 模拟数据加载
        postList.clear();
        postList.add(new Post(
                R.drawable.avatar1,
                "用户1",
                "这是第一条测试动态！",
                R.drawable.post_image1
        ));
        postList.add(new Post(
                R.drawable.avatar2,
                "用户2",
                "今天天气真好☀️",
                R.drawable.post_image2
        ));
        adapter.notifyDataSetChanged();
    }
}