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
import android.widget.Toast;

import com.example.pa.R;
import com.example.pa.util.checkLogin;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.example.pa.data.cloudRepository.GroupRepository;
import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.model.post.PostResponse;
import com.example.pa.data.model.post.Post;

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
    private GroupRepository groupRepository;

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

        // 初始化 GroupRepository
        groupRepository = new GroupRepository();

        // 用户已登录，显示帖子列表
        recyclerView.setVisibility(View.VISIBLE);
        notLoggedInText.setVisibility(View.GONE);
        
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postList = new ArrayList<>();
        filteredPostList = new ArrayList<>();
        
        // 加载用户已加入的群组
        loadUserGroups();

        return view;
    }

    private void loadUserGroups() {
        groupRepository.getJoinedGroups(new GroupRepository.GroupCallback<List<GroupInfo>>() {
            @Override
            public void onSuccess(List<GroupInfo> groups) {
                if (groups.isEmpty()) {
                    Toast.makeText(getContext(), "您还没有加入任何群组", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 创建群组标签
                createGroupChips(groups);
                
                // 加载第一个群组的帖子
                if (!groups.isEmpty()) {
                    loadGroupPosts(groups.get(0).getId());
                }
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "获取群组列表失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroupChips(List<GroupInfo> groups) {
        groupChipGroup.removeAllViews();

        // 创建"全部"标签
        Chip allChip = createChip("全部");
        allChip.setChecked(true);
        groupChipGroup.addView(allChip);

        // 为每个群组创建标签
        for (GroupInfo group : groups) {
            Chip chip = createChip(group.getName());
            chip.setTag(group.getId()); // 存储群组ID
            groupChipGroup.addView(chip);
        }

        // 设置标签选择监听器
        groupChipGroup.setOnCheckedChangeListener((group, checkedId) -> {
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                String selectedGroup = selectedChip.getText().toString();
                if (selectedGroup.equals("全部")) {
                    // 显示所有群组的帖子
                    loadAllGroupPosts(groups);
                } else {
                    // 加载选中群组的帖子
                    String groupId = (String) selectedChip.getTag();
                    loadGroupPosts(groupId);
                }
            }
        });
    }

    private void loadGroupPosts(String groupId) {
        groupRepository.getGroupPosts(groupId, new GroupRepository.GroupCallback<List<Post>>() {
            @Override
            public void onSuccess(List<Post> posts) {
                postList.clear();
                for (Post post : posts) {
                    // 使用Post类中的正确字段
                    String[] imageUrls = post.getImageUrls();
                    String imageUrl = imageUrls != null && imageUrls.length > 0 ? imageUrls[0] : null;
                    
                    if (imageUrl != null) {
                        // 使用URL构造函数
                        postList.add(new SocialPost(
                            String.valueOf(post.getId()), // 临时使用ID作为用户名
                            "分享了一张照片", // 临时使用固定文本
                            imageUrl,
                            groupId // 临时使用groupId作为群组名
                        ));
                    } else {
                        // 使用资源ID构造函数
                        postList.add(new SocialPost(
                            String.valueOf(post.getId()),
                            "分享了一张照片",
                            R.drawable.sample_image,
                            groupId
                        ));
                    }
                }
                updatePostList();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(getContext(), "获取群组帖子失败: " + errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadAllGroupPosts(List<GroupInfo> groups) {
        postList.clear();
        for (GroupInfo group : groups) {
            loadGroupPosts(group.getId());
        }
    }

    private void updatePostList() {
        filteredPostList.clear();
        filteredPostList.addAll(postList);
        if (adapter == null) {
            adapter = new SocialPostAdapter(filteredPostList);
            recyclerView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
    }

    private Chip createChip(String text) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setClickable(true);
        return chip;
    }
}
