// UserApiService.java
package com.example.pa.data.network;

import com.example.pa.data.model.user.*;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApiService {
    /***
     * 用户注册
     * <p>
     * 注册一个新用户
     */
    @POST("user/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("user/register")
    Observable<RegisterResponse> registerRx(@Body RegisterRequest request);

    /***
     * 获取用户信息
     * <p>
     * 登录并获取用户信息和token
     */
    @GET("user/info")
    Call<UserInfoResponse> getUserInfo();

    @GET("user/info")
    Observable<UserInfoResponse> getUserInfoRx();

    /***
     * 用户登录
     * <p>
     * 执行登录操作
     */

    @POST("user/login")
    Call<LoginResponse> login(@Body LoginRequest request);
    @POST("user/login")
    Observable<LoginResponse> loginRx(@Body LoginRequest request);





}