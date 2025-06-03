package com.example.pa.data.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;



// 设置Retrofit客户端
// RetrofitClient.java
public class RetrofitClient {
    private static final String BASE_URL = "http://172.18.36.66:8081/"; // 替换为您服务器的实际IP和端口
    private static RetrofitClient instance;
    private final Retrofit retrofit;

    private PhotoApiService photoApiService;
    private UserApiService userApiService;
    private PostApiService postApiService;

    private RetrofitClient() {
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create()) // 添加RxJava支持
                .build();

        //photoApiService = retrofit.create(PhotoApiService.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    // 获取PhotoApiService实例
    public PhotoApiService getPhotoApiService() {
        if (photoApiService == null) {
            photoApiService = retrofit.create(PhotoApiService.class);
        }
        return photoApiService;
    }

    // 新增获取UserApiService实例的方法
    public UserApiService getUserApiService() {
        if (userApiService == null) {
            userApiService = retrofit.create(UserApiService.class);
        }
        return userApiService;
    }

    public PostApiService getPostApiService() {
        if (postApiService == null) {
            postApiService = retrofit.create(PostApiService.class);
        }
        return postApiService;
    }

    public static String getBaseUrl() {
        return BASE_URL;
    }
}