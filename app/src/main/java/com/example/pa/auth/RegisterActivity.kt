package com.example.pa.auth

import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pa.MyApplication
import com.example.pa.R
import com.example.pa.data.cloudRepository.UserRepository
import com.example.pa.data.cloudRepository.UserRepository.UserCallback
import com.example.pa.data.model.user.RegisterResponse
import com.example.pa.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private var binding: ActivityRegisterBinding? = null
    private var userRepository: UserRepository? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        userRepository = MyApplication.getInstance().userRepository // 初始化用户仓库
        setContentView(binding!!.root)

        // 设置注册按钮点击事件
        binding!!.registerButton.setOnClickListener { v: View? -> attemptRegister() }

        // 设置登录文本点击事件
        binding!!.loginLink.setOnClickListener { v: View? -> finish() }
    }

    private fun attemptRegister() {
        // 从TextInputEditText获取输入
        val username = binding!!.etUsername.editText!!.text.toString().trim { it <= ' ' }
        val email = binding!!.etEmail.editText!!.text.toString().trim { it <= ' ' }
        val password = binding!!.etPassword.editText!!.text.toString().trim { it <= ' ' }
        val confirmPassword =
            binding!!.etConfirmPassword.editText!!.text.toString().trim { it <= ' ' }

        // 简单验证
        if (username.isEmpty()) {
            binding!!.etUsername.error = "Please enter username"
            return
        } else {
            binding!!.etUsername.error = null
        }
        if (email.isEmpty()) {
            binding!!.etEmail.error = "Please enter email"
            return
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding!!.etEmail.error = "Please enter a valid email"
            return
        } else {
            binding!!.etEmail.error = null
        }

        if (password.isEmpty()) {
            binding!!.etPassword.error = "Please enter password"
            return
        } else if (password.length < 6) {
            binding!!.etPassword.error = "Password must be at least 6 characters"
            return
        } else if (!password.matches(".*[A-Z].*".toRegex())) {
            binding!!.etPassword.error = "Password must contain at least one uppercase letter"
            return
        } else if (!password.matches(".*[a-z].*".toRegex())) {
            binding!!.etPassword.error = "Password must contain at least one lowercase letter"
            return
        } else if (!password.matches(".*\\d.*".toRegex())) {
            binding!!.etPassword.error = "Password must contain at least one digit"
            return
        } else {
            binding!!.etPassword.error = null
        }

        if (password != confirmPassword) {
            binding!!.etConfirmPassword.error = "Passwords do not match"
            return
        } else {
            binding!!.etConfirmPassword.error = null
        }

        // 显示加载中
        binding!!.registerButton.isEnabled = false
        binding!!.registerButton.setText(R.string.registering)

        userRepository!!.register(
            username,
            email,
            password,
            object : UserCallback<RegisterResponse?> {
                override fun onSuccess(result: RegisterResponse?) {
                    if (result != null) {
                        Toast.makeText(
                            this@RegisterActivity,
                            R.string.register_success,
                            Toast.LENGTH_SHORT
                        ).show()
                        binding!!.registerButton.isEnabled = true
                        binding!!.registerButton.setText(R.string.register)
                        finish()
                    } else {
                        Toast.makeText(
                            this@RegisterActivity,
                            "Registration failed, response is null",
                            Toast.LENGTH_SHORT
                        ).show()
                        binding!!.registerButton.isEnabled = true
                        binding!!.registerButton.setText(R.string.register)
                    }
                }

                override fun onError(errorMessage: String) {
                    Toast.makeText(this@RegisterActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    binding!!.registerButton.isEnabled = true
                    binding!!.registerButton.setText(R.string.register)
                }
            })
    }
}