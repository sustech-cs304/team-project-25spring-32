package com.example.pa.data.network;

import android.database.Observable;

import com.example.pa.data.model.UploadResponse;
import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.model.group.GroupOperationResponse;
import com.example.pa.data.model.group.JoinGroupRequest;
import com.example.pa.data.model.post.Post;

import java.util.List;

//import io.reactivex.rxjava3.core.Observable;
import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroupApiService {

    @POST("groups")
    Call<GroupInfo> createGroup(
            @Body GroupInfo groupInfo
    );

    // 添加群组照片上传接口
    @Multipart
    @POST("groups/{groupId}/photos")
    Call<UploadResponse> uploadGroupPhoto(
            @Path("groupId") String groupId,
            @Part MultipartBody.Part photo
    );

    // 获取群组照片列表
    @GET("groups/{groupId}/photos")
    Call<List<String>> getGroupPhotos(@Path("groupId") String groupId);

    @POST("groups/{groupId}/join")
    Call<GroupOperationResponse> joinGroup(
            @Path("groupId") String groupId,
            @Body JoinGroupRequest request
    );

    @POST("groups/{groupId}/join")
    Observable<GroupOperationResponse> joinGroupRx(
            @Path("groupId") String groupId,
            @Body JoinGroupRequest request
    );

    // 获取当前用户已加入的群组
    @GET("users/me/groups")
    Call<List<GroupInfo>> getJoinedGroups();

    @GET("users/me/groups")
    Observable<List<GroupInfo>> getJoinedGroupsRx();

    // 获取可加入的公开群组
    @GET("groups/public")
    Call<List<GroupInfo>> getAvailableGroups(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("groups/public")
    Observable<List<GroupInfo>> getAvailableGroupsRx(
            @Query("page") int page,
            @Query("size") int size
    );

    // 退出群组
    @POST("groups/{groupId}/leave")
    Call<GroupOperationResponse> leaveGroup(@Path("groupId") String groupId);

    @POST("groups/{groupId}/leave")
    Observable<GroupOperationResponse> leaveGroupRx(@Path("groupId") String groupId);

    @GET("groups/{groupId}/posts")
    Call<List<Post>> getGroupPosts(@Path("groupId") String groupId);

    @GET("groups/{groupId}/posts")
    Observable<List<Post>> getGroupPostsRx(@Path("groupId") String groupId);
}
