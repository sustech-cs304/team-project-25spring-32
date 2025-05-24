// UserApiService.java
package com.example.pa.data.network;

import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.model.group.GroupOperationResponse;
import com.example.pa.data.model.group.JoinGroupRequest;
import com.example.pa.data.model.user.*;

import java.util.List;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.*;

public interface UserApiService {
    @POST("user/register")
    Call<RegisterResponse> register(@Body RegisterRequest request);

    @POST("user/register")
    Observable<RegisterResponse> registerRx(@Body RegisterRequest request);

    @GET("user/info")
    Call<UserInfoResponse> getUserInfo();

    @GET("user/info")
    Observable<UserInfoResponse> getUserInfoRx();

    @PUT("user/info")
    Call<UpdateUserResponse> updateUserInfo(@Body UpdateUserRequest request);

    @PUT("user/info")
    Observable<UpdateUserResponse> updateUserInfoRx(@Body UpdateUserRequest request);

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


}