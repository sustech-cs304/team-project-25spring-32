package com.example.pa.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pa.MyApplication
import com.example.pa.R
import com.example.pa.data.cloudRepository.UserRepository
import com.example.pa.data.cloudRepository.UserRepository.UserCallback
import com.example.pa.data.model.user.LoginResponse
import com.example.pa.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private var binding: ActivityLoginBinding? = null
    private var userRepository: UserRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        userRepository = MyApplication.getInstance().userRepository // 初始化用户仓库
        setContentView(binding!!.root)

        // 设置登录按钮点击事件
        binding!!.loginButton.setOnClickListener { v: View? -> attemptLogin() }
        // 设置返回按钮点击事件
        binding!!.backButton.setOnClickListener { view: View? -> finish() }

        // 设置注册文本点击事件
        binding!!.registerLink.setOnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    RegisterActivity::class.java
                )
            )
            overridePendingTransition(
                R.anim.slide_in_right,
                R.anim.slide_out_left
            )
        }

        // 设置忘记密码点击事件
        binding!!.forgotPassword.setOnClickListener { v: View? ->
            // 这里添加忘记密码逻辑
            Toast.makeText(this, "忘记密码功能待实现", Toast.LENGTH_SHORT).show()
        }
    }

    private fun attemptLogin() {
        // 从TextInputEditText获取输入
        val username = binding!!.etUsername.editText!!.text.toString().trim { it <= ' ' }
        val password = binding!!.etPassword.editText!!.text.toString().trim { it <= ' ' }

        // 简单验证
        if (username.isEmpty()) {
            binding!!.etUsername.error = "请输入用户名。"
            return
        } else {
            binding!!.etUsername.error = null
        }

        if (password.isEmpty()) {
            binding!!.etPassword.error = "请输入密码"
            return
        } else {
            binding!!.etPassword.error = null
        }

        // 显示加载中
        binding!!.loginButton.isEnabled = false
        binding!!.loginButton.setText(R.string.logging_in)

        userRepository!!.login(username, password, object : UserRepository.UserCallback<LoginResponse?> {
            override fun onSuccess(response: LoginResponse?) {
                if (response != null) {
                    val sharedPreferences = getSharedPreferences("auth_prefs", MODE_PRIVATE)
                    val editor = sharedPreferences.edit()
                    editor.putString("auth_token", response.token)
                    editor.putString("username", username)
                    editor.putBoolean("is_logged_in", true)
                    editor.apply()

                    Toast.makeText(this@LoginActivity, R.string.login_success, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_OK)
                    finish()
                } else {
                    Toast.makeText(this@LoginActivity, "登录失败，响应为空", Toast.LENGTH_SHORT).show()
                    binding!!.loginButton.isEnabled = true
                    binding!!.loginButton.setText(R.string.login)
                }
            }

            override fun onError(errorMessage: String) {
                Toast.makeText(this@LoginActivity, errorMessage, Toast.LENGTH_SHORT).show()
                binding!!.loginButton.isEnabled = true
                binding!!.loginButton.setText(R.string.login)
            }
        })
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResult(RESULT_CANCELED)
        overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right)
    }
}