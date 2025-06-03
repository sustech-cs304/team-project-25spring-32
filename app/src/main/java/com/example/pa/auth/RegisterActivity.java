package com.example.pa.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.MyApplication;
import com.example.pa.R;
import com.example.pa.data.cloudRepository.UserRepository;
import com.example.pa.data.model.user.RegisterResponse;
import com.example.pa.databinding.ActivityRegisterBinding;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        userRepository = MyApplication.getInstance().getUserRepository(); // 初始化用户仓库
        setContentView(binding.getRoot());

        // 设置注册按钮点击事件
        binding.registerButton.setOnClickListener(v -> attemptRegister());

        // 设置登录文本点击事件
        binding.loginLink.setOnClickListener(v -> finish());
    }

    private void attemptRegister() {
        // 从TextInputEditText获取输入
        String username = binding.etUsername.getEditText().getText().toString().trim();
        String email = binding.etEmail.getEditText().getText().toString().trim();
        String password = binding.etPassword.getEditText().getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getEditText().getText().toString().trim();

        // 简单验证
        if (username.isEmpty()) {
            binding.etUsername.setError("Please enter username");
            return;
        } else {
            binding.etUsername.setError(null);
        }
        if (email.isEmpty()) {
            binding.etEmail.setError("Please enter email");
            return;
        } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            return;
        } else {
            binding.etEmail.setError(null);
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Please enter password");
            return;
        } else if (password.length()< 6) {
            binding.etPassword.setError("Password must be at least 6 characters");
            return;
        } else if (!password.matches(".*[A-Z].*")) {
            binding.etPassword.setError("Password must contain at least one uppercase letter");
            return;
        } else if (!password.matches(".*[a-z].*")) {
            binding.etPassword.setError("Password must contain at least one lowercase letter");
            return;
        } else if (!password.matches(".*\\d.*")) {
            binding.etPassword.setError("Password must contain at least one digit");
            return;
        } else {
            binding.etPassword.setError(null);
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            return;
        } else {
            binding.etConfirmPassword.setError(null);
        }

        // 显示加载中
        binding.registerButton.setEnabled(false);
        binding.registerButton.setText(R.string.registering);

        userRepository.register(username, email, password, new UserRepository.UserCallback<RegisterResponse>() {
            @Override
            public void onSuccess(RegisterResponse result) {
                Toast.makeText(RegisterActivity.this, R.string.register_success, Toast.LENGTH_SHORT).show();
                binding.registerButton.setEnabled(true);
                binding.registerButton.setText(R.string.register);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                Toast.makeText(RegisterActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                binding.registerButton.setEnabled(true);
                binding.registerButton.setText(R.string.register);
            }
        });
    }
}