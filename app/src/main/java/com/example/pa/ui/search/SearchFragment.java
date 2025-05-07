package com.example.pa.ui.search;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.pa.R;
import com.example.pa.databinding.FragmentSearchBinding;
import com.example.pa.ui.photo.PhotoDetailActivity;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: 生成整体模板，改动了部分参数和变化逻辑.
     *
     */

    private FragmentSearchBinding binding;
    private SearchViewModel searchViewModel;
    private ImageAdapter imageAdapter;
    private List<String> suggestions;
    private ArrayAdapter<String> suggestionAdapter;
    private FlexboxLayout flexboxRecommendations;

    @SuppressLint("ClickableViewAccessibility")
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        binding = FragmentSearchBinding.inflate(inflater, container, false);

        ImageView clearButton = binding.clearButton;//清除按钮

        View root = binding.getRoot();

        flexboxRecommendations = binding.flexboxRecommendations;
        // 加载推荐词
        searchViewModel.loadRecommendations();

        // 观察推荐词变化
        searchViewModel.getRecommendations().observe(getViewLifecycleOwner(), this::updateRecommendations);

        final EditText searchBox = binding.searchBox;
        //updateClearButtonVisibility(false);
        final ListView suggestionList = binding.suggestionList;
        final RecyclerView imageRecyclerView = binding.imageRecyclerView;
        //final ImageView defaultImage = binding.defaultImage;
        //这个是初始默认图片的展示，改成文字描述了
        final TextView descriptionText = binding.descriptionText;

        // 初始化推荐词列表
        suggestions = new ArrayList<>();
        suggestionAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, suggestions);
        suggestionList.setAdapter(suggestionAdapter);

        // 初始化图片展示 RecyclerView
        imageRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3)); // 3列网格布局
        imageAdapter = new ImageAdapter(new ArrayList<>());
        imageRecyclerView.setAdapter(imageAdapter);

        // 点击展示大图
        imageAdapter.setOnImageClickListener(imagePath -> {
            Context context = getContext();
            if (context != null) {
                Intent intent = new Intent(context, PhotoDetailActivity.class);
                intent.putExtra("image_path", imagePath);
                startActivity(intent);

                if (getActivity() != null) {
                    getActivity().overridePendingTransition(
                            android.R.anim.fade_in,
                            android.R.anim.fade_out
                    );
                }
            }
        });

        // 监听输入框内容变化
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    // 如果输入框为空，显示默认图片并隐藏 RecyclerView
                    //defaultImage.setVisibility(View.VISIBLE);
                    descriptionText.setVisibility(View.VISIBLE);
                    imageRecyclerView.setVisibility(View.GONE);
                    suggestionList.setVisibility(View.GONE); // 隐藏推荐框
                } else {
                    // 如果输入框有内容，隐藏默认图片并显示 RecyclerView
                    //defaultImage.setVisibility(View.GONE);
                    descriptionText.setVisibility(View.GONE);
                    imageRecyclerView.setVisibility(View.VISIBLE);
                    suggestionList.setVisibility(View.VISIBLE); // 显示推荐框
                    //这里下个sprint实现

                    // 更新推荐词列表
                    searchViewModel.updateSuggestions(s.toString());
                    // 触发搜索逻辑
                    searchViewModel.searchImages(s.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });



        // 修改原有TextWatcher，添加图标状态管理
        searchBox.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 更新右侧清除按钮可见性
                //updateClearButtonVisibility(s.length() > 0);

                clearButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                //控制清除按钮的显示与隐藏


                if (s.length() == 0) {
                    //defaultImage.setVisibility(View.VISIBLE);
                    descriptionText.setVisibility(View.VISIBLE);
                    imageRecyclerView.setVisibility(View.GONE);
                    suggestionList.setVisibility(View.GONE);
                } else {
                    //defaultImage.setVisibility(View.GONE);
                    descriptionText.setVisibility(View.GONE);
                    imageRecyclerView.setVisibility(View.VISIBLE);
                    suggestionList.setVisibility(View.VISIBLE);
                    searchViewModel.updateSuggestions(s.toString());
                    searchViewModel.searchImages(s.toString());
                }
            }



            @Override
            public void afterTextChanged(Editable s) {}
        });

        clearButton.setOnClickListener(v -> {
            searchBox.setText("");
            descriptionText.setVisibility(View.VISIBLE);
            imageRecyclerView.setVisibility(View.GONE);
            suggestionList.setVisibility(View.GONE);
        });

        // 设置推荐词点击事件
        suggestionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 获取点击的推荐词
                String selectedSuggestion = suggestions.get(position);
                // 将推荐词填入输入框
                searchBox.setText(selectedSuggestion);
                // 触发搜索逻辑
                searchViewModel.searchImages(selectedSuggestion);
                // 隐藏推荐框
                suggestionList.setVisibility(View.GONE);
            }
        });

        // 观察推荐词的变化
        searchViewModel.getSuggestions().observe(getViewLifecycleOwner(), newSuggestions -> {
            suggestions.clear();
            suggestions.addAll(newSuggestions);
            suggestionAdapter.notifyDataSetChanged(); // 通知 Adapter 数据已更新
        });

        // 观察搜索结果图片的变化
        searchViewModel.getSearchResults().observe(getViewLifecycleOwner(), images -> {
            imageAdapter.updateImages(images); // 更新图片列表
        });


        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private void updateRecommendations(List<String> recommendations) {
        flexboxRecommendations.removeAllViews();

        for (String keyword : recommendations) {
            TextView textView = new TextView(getContext());
            textView.setText(keyword);
            textView.setTextSize(18);
            textView.setPadding(16, 8, 16, 8);
            textView.setBackgroundResource(R.drawable.recommendation_bg);
            textView.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

            FlexboxLayout.LayoutParams params = new FlexboxLayout.LayoutParams(
                    // 宽高包裹内容
                    // 这两行代码不是重复的，是设置宽高的参数，哈哈哈哈，笑嘻了
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 8, 8);
            textView.setLayoutParams(params);

            // 点击推荐词时，填入搜索框并搜索
            textView.setOnClickListener(v -> {
                binding.searchBox.setText(keyword);
                binding.searchBox.setSelection(keyword.length());
                searchViewModel.searchImages(keyword);
            });
            //TODO:是不是还要完善一下点击推荐词后关闭补全框的功能

            flexboxRecommendations.addView(textView);
        }
    }

}