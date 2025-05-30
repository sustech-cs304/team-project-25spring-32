package com.example.pa.auth;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.pa.R;
import com.example.pa.databinding.ActivityLoginBinding;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 设置登录按钮点击事件
        binding.loginButton.setOnClickListener(v -> attemptLogin());

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
        binding.loginButton.setText("登录中...");

        // 模拟网络请求
        new Handler().postDelayed(() -> {
            boolean loginSuccess = true; // 模拟登录成功

            if (loginSuccess) {
                // 保存登录状态
                SharedPreferences prefs = getSharedPreferences("auth_prefs", MODE_PRIVATE);
                prefs.edit()
                        .putBoolean("is_logged_in", true)
                        .putString("username", username)
                        .apply();

                // 返回成功结果
                Intent result = new Intent();
                result.putExtra("username", username);
                setResult(RESULT_OK, result);
                finish();
            } else {
                // 登录失败
                binding.loginButton.setEnabled(true);
                binding.loginButton.setText("登 录");
                Toast.makeText(LoginActivity.this, "登录失败，请检查凭证", Toast.LENGTH_SHORT).show();
            }
        }, 1500);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED);
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
    }
}