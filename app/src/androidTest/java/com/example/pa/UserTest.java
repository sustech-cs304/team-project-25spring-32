package com.example.pa;

import static com.example.pa.util.removeLogin.removeLoginStatus;
import static org.junit.Assert.assertFalse;

import android.content.Context;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pa.data.cloudRepository.UserRepository;
import com.example.pa.data.model.user.LoginResponse;
import com.example.pa.data.model.user.RegisterResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserTest {
    private MyApplication app;
    UserRepository userRepository;

    @Before //这个就是测试前的准备工作
    public void setUp() {
        // 获取 Application 实例, 这个一定要有
        app = (MyApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        // 初始化 UserRepository
        userRepository = new UserRepository(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    /**     * 测试用户相关功能
     * 注意：此测试需要网络权限和正确的服务器配置才能成功执行
     *
     *
     */

    @Test
    public void test_registerRx(){
        userRepository.registerRx("wang","123@qq.com","123456");
    }

    @Test
    public void test_register(){
        userRepository.register("wang", "123@qq.com", "123456",
                new UserRepository.UserCallback<RegisterResponse>() {
                    @Override
                    public void onSuccess(RegisterResponse result) {
                        // 只判断成功状态
                        if (result.isSuccess()) {
                            System.out.println("注册成功");
                            Log.d("success", "注册成功: ");
                        } else {
                            System.out.println("注册失败: " + result.getMessage());
                            Log.d("fail", "注册失败: " + result.getMessage());
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        // 网络错误等异常
                        System.err.println("请求失败: " + errorMessage);
                        Log.e("error", "请求失败: " + errorMessage);
                    }
                });
    }

    @Test
    public void test_login() throws InterruptedException {
        // 测试登录功能
        userRepository.login("wang", "123456",
                new UserRepository.UserCallback<>() {
                    @Override
                    public void onSuccess(LoginResponse result) {
                        if (result.isSuccess()) {
                            System.out.println("登录成功");
                            Log.d("success", "登录成功: ");
                        } else {
                            System.out.println("登录失败: " + result.getMessage());
                            Log.d("fail", "登录失败: " + result.getMessage());
                        }
                    }

                    @Override
                    public void onError(String errorMessage) {
                        System.err.println("请求失败: " + errorMessage);
                        Log.e("error", "请求失败: " + errorMessage);
                    }
                });
        Thread.sleep(5000);
    }

    @Test
    public void test_logout() {
        // 测试登出功能
        userRepository.logout();
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        boolean isLoggedIn = com.example.pa.util.checkLogin.checkLoginStatus(context);
        assertFalse("User should be logged out", isLoggedIn);
    }

    @Test
    public void test_removeLogin(){
        // 测试移除登录状态
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", false)
                .apply();
        removeLoginStatus(context);
        boolean isLoggedIn = com.example.pa.util.checkLogin.checkLoginStatus(context);
        assertFalse("User should be logged out after removing login status", isLoggedIn);
    }
}
