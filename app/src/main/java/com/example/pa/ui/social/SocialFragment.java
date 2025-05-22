package com.example.pa.ui.social;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.pa.R;

import java.util.ArrayList;
import java.util.List;

public class SocialFragment extends Fragment {

    private RecyclerView recyclerView;
    private SocialPostAdapter adapter;
    private List<SocialPost> postList;

    public SocialFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_social, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewPosts);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        postList = new ArrayList<>();
        // 示例数据
        postList.add(new SocialPost("用户A", "这是用户A的文字内容", R.drawable.sample_image));
        postList.add(new SocialPost("用户B", "看看这张照片～", R.drawable.sample_image));

        adapter = new SocialPostAdapter(postList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
