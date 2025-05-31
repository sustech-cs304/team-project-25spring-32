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

import com.example.pa.R;

import java.io.IOException;

public class PostCreateFragment extends Fragment {

    private static final int REQUEST_IMAGE_PICK = 1001;

    private EditText editTextContent;
    private Button buttonSelectImage;
    private ImageView imagePreview;
    private Button buttonPost;

    private Uri selectedImageUri = null;

    public PostCreateFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_post_create, container, false);

        editTextContent = view.findViewById(R.id.editTextContent);
        buttonSelectImage = view.findViewById(R.id.buttonSelectImage);
        imagePreview = view.findViewById(R.id.imagePreview);
        buttonPost = view.findViewById(R.id.buttonPost);

        buttonSelectImage.setOnClickListener(v -> openImagePicker());

        buttonPost.setOnClickListener(v -> publishPost());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, REQUEST_IMAGE_PICK);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_IMAGE_PICK && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                imagePreview.setImageURI(selectedImageUri);
                imagePreview.setVisibility(View.VISIBLE);
            }
        }
    }

    private void publishPost() {
        String content = editTextContent.getText().toString().trim();

        if (content.isEmpty() && selectedImageUri == null) {
            Toast.makeText(getContext(), "请输入内容或选择一张照片", Toast.LENGTH_SHORT).show();
            return;
        }

        // 这里你可以添加上传逻辑，比如上传图片到服务器，发布内容到数据库
        // 先简单Toast提示模拟发布成功
        Toast.makeText(getContext(), "发布成功！", Toast.LENGTH_SHORT).show();

        // 清空内容和图片
        editTextContent.setText("");
        imagePreview.setImageDrawable(null);
        imagePreview.setVisibility(View.GONE);
        selectedImageUri = null;
    }
}
