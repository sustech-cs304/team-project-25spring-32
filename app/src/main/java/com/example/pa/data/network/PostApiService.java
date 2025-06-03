package com.example.pa.data.network;

import com.example.pa.data.model.post.CreatePostRequest;
import com.example.pa.data.model.post.PostResponse;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.*;

public interface PostApiService {
    /**
     * 上传图片到指定群组
     */
    @POST("groups/{groupId}/posts")
    Call<PostResponse> uploadImages(
            @Path("groupId") String groupId,
            @Body CreatePostRequest request
    );

    @POST("groups/{groupId}/posts")
    Observable<PostResponse> uploadImagesRx(
            @Path("groupId") String groupId,
            @Body CreatePostRequest request
    );

    /**
     * 获取指定群组的图片列表
     */
    @GET("groups/{groupId}/posts")
    Call<PostResponse> getImages(@Path("groupId") String groupId);

    @GET("groups/{groupId}/posts")
    Observable<PostResponse> getImagesRx(@Path("groupId") String groupId);
} 