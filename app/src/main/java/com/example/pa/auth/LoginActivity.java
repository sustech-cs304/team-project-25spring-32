package com.example.pa.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.MyApplication;
import com.example.pa.R;
import com.example.pa.data.cloudRepository.UserRepository;
import com.example.pa.data.model.user.LoginResponse;
import com.example.pa.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private UserRepository userRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        userRepository = MyApplication.getInstance().getUserRepository(); // 初始化用户仓库
        setContentView(binding.getRoot());

        // 设置登录按钮点击事件
        binding.loginButton.setOnClickListener(v -> attemptLogin());
        // 设置返回按钮点击事件
        binding.backButton.setOnClickListener(view -> finish());

        // 设置注册文本点击事件
        binding.registerLink.setOnClickListener(v -> {
            startActivity(new Intent(this, RegisterActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // 设置忘记密码点击事件
        binding.forgotPassword.setOnClickListener(v -> {
            // 这里添加忘记密码逻辑
            Toast.makeText(this, "忘记密码功能待实现", Toast.LENGTH_SHORT).show();
        });
    }

    private void attemptLogin() {
        // 从TextInputEditText获取输入
        String username = binding.etUsername.getEditText().getText().toString().trim();
        String password = binding.etPassword.getEditText().getText().toString().trim();

        // 简单验证
        if (username.isEmpty()) {
            binding.etUsername.setError("请输入用户名");
            return;
        } else {
            binding.etUsername.setError(null);
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("请输入密码");
            return;
        } else {
            binding.etPassword.setError(null);
        }

        // 显示加载中
        binding.loginButton.setEnabled(false);
        binding.loginButton.setText(R.string.logging_in);

        userRepository.login(username, password, new UserRepository.UserCallback<LoginResponse>() {
            @Override
            public void onSuccess(LoginResponse response) {
                // 登录成功，保存Token
                SharedPreferences sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("auth_token", response.getToken());
                editor.putBoolean("is_logged_in", true);
                editor.apply();

                // 显示成功消息并跳转到主界面
                Toast.makeText(LoginActivity.this, R.string.login_success, Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            }

            @Override
            public void onError(String errorMessage) {
                // 登录失败，显示错误消息
                Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText(R.string.login);
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}