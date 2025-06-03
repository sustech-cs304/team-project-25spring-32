package com.example.pa.data.network;

import com.example.pa.data.model.Photo;
import com.example.pa.data.model.UploadResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

// 创建Retrofit服务接口
// PhotoApiService.java
public interface PhotoApiService {
    @GET("photos")
    Call<List<Photo>> getPhotos();

    @Multipart
    @POST("upload")
    Call<UploadResponse> uploadPhoto(@Part MultipartBody.Part photo);

    @DELETE("photos/{filename}")
    Call<Map<String, String>> deletePhoto(@Path("filename") String filename);
}