package com.example.pa.data.network;

import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.model.group.GroupOperationResponse;
import com.example.pa.data.model.group.JoinGroupRequest;

import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface GroupApiService {
    /***
     * 加入群组
     * <p>
     *
     */

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
}
