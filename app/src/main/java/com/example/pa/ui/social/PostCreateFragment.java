package com.example.pa.ui.social;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pa.R;

public class PostCreateFragment extends Fragment {

    private EditText editTextContent;
    private Button buttonPost;
    private ImageView imagePreview;
    private Uri selectedImageUri = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_create, container, false);

        editTextContent = view.findViewById(R.id.editTextContent);
        buttonPost = view.findViewById(R.id.buttonPost);
        imagePreview = view.findViewById(R.id.imagePreview);

        // 获取传入的图片 URI
        Bundle args = getArguments();
        if (args != null) {
            selectedImageUri = args.getParcelable("imageUri");
            if (selectedImageUri != null) {
                // 显示图片预览
                Glide.with(this)
                    .load(selectedImageUri)
                    .into(imagePreview);
                imagePreview.setVisibility(View.VISIBLE);
            }
        }

        buttonPost.setOnClickListener(v -> publishPost());

        return view;
    }

    private void publishPost() {
        String content = editTextContent.getText().toString().trim();

        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(getContext(), "请输入内容或选择一张照片", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: 实现发布帖子的逻辑
        // 这里可以调用 API 上传图片和内容
        Toast.makeText(getContext(), "发布成功！", Toast.LENGTH_SHORT).show();

        // 清空内容和图片
        editTextContent.setText("");
        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);
        selectedImageUri = null;

        // 关闭当前页面
        if (getActivity() != null) {
            getActivity().finish();
        }
    }
}
