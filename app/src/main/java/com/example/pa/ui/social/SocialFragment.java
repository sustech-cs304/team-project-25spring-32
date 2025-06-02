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
import android.widget.TextView;

import com.example.pa.R;
import com.example.pa.util.checkLogin;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SocialFragment extends Fragment {

    private RecyclerView recyclerView;
    private SocialPostAdapter adapter;
    private List<SocialPost> postList;
    private List<SocialPost> filteredPostList;
    private TextView notLoggedInText;
    private ChipGroup groupChipGroup;
    private String currentSelectedGroup = null;

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
        notLoggedInText = view.findViewById(R.id.notLoggedInText);
        groupChipGroup = view.findViewById(R.id.groupChipGroup);

        // 用户已登录，显示帖子列表
        recyclerView.setVisibility(View.VISIBLE);
        notLoggedInText.setVisibility(View.GONE);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        filteredPostList = new ArrayList<>();
        
        // 示例数据 - 按群组分类
        // 群组1的帖子
        postList.add(new SocialPost("用户A", "这是群组1的第一条帖子", R.drawable.sample_image, "学习交流群"));
        postList.add(new SocialPost("用户B", "群组1的分享", R.drawable.sample_image, "学习交流群"));
        
        // 群组2的帖子
        postList.add(new SocialPost("用户C", "这是群组2的帖子", R.drawable.sample_image, "运动健身群"));
        postList.add(new SocialPost("用户D", "群组2的日常分享", R.drawable.sample_image, "运动健身群"));
        
        // 群组3的帖子
        postList.add(new SocialPost("用户E", "群组3的讨论", R.drawable.sample_image, "美食分享群"));
        postList.add(new SocialPost("用户F", "群组3的美食分享", R.drawable.sample_image, "美食分享群"));

        // 群组4的帖子
        postList.add(new SocialPost("用户E", "群组4的讨论", R.drawable.sample_image, "分享群"));
        postList.add(new SocialPost("用户F", "群组4的美食分享", R.drawable.sample_image, "分享群"));

        // 创建群组标签
        createGroupChips();
        
        // 初始化显示所有帖子
        filteredPostList.addAll(postList);
        adapter = new SocialPostAdapter(filteredPostList);
        recyclerView.setAdapter(adapter);

        return view;
    }

    private void createGroupChips() {
        // 获取所有不重复的群组名称
        Set<String> groupNames = new HashSet<>();
        for (SocialPost post : postList) {
            groupNames.add(post.getGroupName());
        }

        // 创建"全部"标签
        Chip allChip = createChip("全部");
        allChip.setChecked(true);
        groupChipGroup.addView(allChip);

        // 为每个群组创建标签
        for (String groupName : groupNames) {
            Chip chip = createChip(groupName);
            groupChipGroup.addView(chip);
        }

        // 设置标签选择监听器
        groupChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                String selectedGroup = selectedChip.getText().toString();
                filterPosts(selectedGroup);
            }
        });
    }

    private Chip createChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);
        return chip;
    }

    private void filterPosts(String groupName) {
        filteredPostList.clear();
        if (groupName.equals("全部")) {
            filteredPostList.addAll(postList);
        } else {
            for (SocialPost post : postList) {
                if (post.getGroupName().equals(groupName)) {
                    filteredPostList.add(post);
                }
            }
        }
        adapter.notifyDataSetChanged();
    }
}
