package com.example.pa.data.cloudRepository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pa.data.model.user.*;
import com.example.pa.data.network.UserApiService;
import com.example.pa.data.network.RetrofitClient;
import com.google.gson.Gson;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private final UserApiService apiService;
    private final SharedPreferences sharedPreferences;

    public UserRepository(Context context) {
        this.apiService = RetrofitClient.getInstance().getUserApiService();
        this.sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
    }

    public interface UserCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    // ==================== 认证相关 ====================

    /**
     * 用户注册（回调方式）
     */
    public void register(String username, String email, String password, UserCallback<RegisterResponse> callback) {
        RegisterRequest request = new RegisterRequest(username, email, password);
        apiService.register(request).enqueue(new Callback<RegisterResponse>() {
            @Override
            public void onResponse(Call<RegisterResponse> call, Response<RegisterResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveAuthToken(response.body().getToken());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("注册失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<RegisterResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 用户注册（RxJava方式）
     */
    public Observable<RegisterResponse> registerRx(String username, String email, String password) {
        return apiService.registerRx(new RegisterRequest(username, email, password))
                .doOnNext(response -> saveAuthToken(response.getToken()))
                .subscribeOn(Schedulers.io());
    }

    /**
     * 退出登录（清除本地凭证）
     */
    public void logout() {
        sharedPreferences.edit()
                .remove("auth_token")
                .remove("current_user")
                .apply();
    }

    // ==================== 用户信息操作 ====================

    /**
     * 获取当前用户信息（回调方式）
     */
    public void fetchUserInfo(UserCallback<UserInfoResponse> callback) {
        apiService.getUserInfo().enqueue(new Callback<UserInfoResponse>() {
            @Override
            public void onResponse(Call<UserInfoResponse> call, Response<UserInfoResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserInfo(response.body().getUser());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("获取用户信息失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UserInfoResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 获取当前用户信息（RxJava方式）
     */
    public Observable<UserInfoResponse> fetchUserInfoRx() {
        return apiService.getUserInfoRx()
                .doOnNext(response -> saveUserInfo(response.getUser()))
                .subscribeOn(Schedulers.io());
    }

    /**
     * 更新用户信息（回调方式）
     */
    public void updateUserInfo(String newUsername, String newEmail, UserCallback<UpdateUserResponse> callback) {
        UpdateUserRequest request = new UpdateUserRequest(newUsername, newEmail);
        apiService.updateUserInfo(request).enqueue(new Callback<UpdateUserResponse>() {
            @Override
            public void onResponse(Call<UpdateUserResponse> call, Response<UpdateUserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    saveUserInfo(response.body().getUpdatedUser());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("更新失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<UpdateUserResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 更新用户信息（RxJava方式）
     */
    public Observable<UpdateUserResponse> updateUserInfoRx(String newUsername, String newEmail) {
        return apiService.updateUserInfoRx(new UpdateUserRequest(newUsername, newEmail))
                .doOnNext(response -> saveUserInfo(response.getUpdatedUser()))
                .subscribeOn(Schedulers.io());
    }

    // ==================== 密码相关操作 ====================

    // ==================== 本地存储方法 ====================

    /**
     * 保存认证Token
     */
    private void saveAuthToken(String token) {
        sharedPreferences.edit()
                .putString("auth_token", token)
                .apply();
    }

    /**
     * 保存用户信息到本地
     */
    private void saveUserInfo(User user) {
        sharedPreferences.edit()
                .putString("current_user", new Gson().toJson(user))
                .apply();
    }

    /**
     * 获取当前保存的用户信息
     */
    public User getCurrentUser() {
        String userJson = sharedPreferences.getString("current_user", null);
        if (userJson != null) {
            return new Gson().fromJson(userJson, User.class);
        }
        return null;
    }

    /**
     * 检查是否已登录
     */
    public boolean isLoggedIn() {
        return sharedPreferences.contains("auth_token");
    }

    /**
     * 获取当前认证Token
     */
    public String getAuthToken() {
        return sharedPreferences.getString("auth_token", null);
    }
}