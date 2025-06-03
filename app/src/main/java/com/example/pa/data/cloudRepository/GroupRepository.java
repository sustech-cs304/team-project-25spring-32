package com.example.pa.data.cloudRepository;

import android.content.Context;
import android.net.Uri;

import com.example.pa.data.model.UploadResponse;
import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.model.group.GroupOperationResponse;
import com.example.pa.data.model.group.JoinGroupRequest;
import com.example.pa.data.network.GroupApiService;
import com.example.pa.data.network.RetrofitClient;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 群组相关API的仓库类
 * 处理与群组相关的网络请求
 */
public class GroupRepository {
    private final GroupApiService apiService;
    private final PhotoRepository photoRepository;

    /**
     * 构造函数
     */
    public GroupRepository() {
        this.apiService = RetrofitClient.getInstance().getGroupApiService();
        this.photoRepository = new PhotoRepository();
    }

    /**
     * 回调接口，用于异步处理API响应
     * @param <T> 响应类型
     */
    public interface GroupCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    /**
     * 创建新群组
     * @param groupInfo 群组信息
     * @param callback 回调接口
     */
    public void createGroup(GroupInfo groupInfo, final GroupCallback<GroupInfo> callback) {
        apiService.createGroup(groupInfo).enqueue(new Callback<GroupInfo>() {
            @Override
            public void onResponse(Call<GroupInfo> call, Response<GroupInfo> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("创建群组失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GroupInfo> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 获取可加入的公开群组
     * @param page 页码
     * @param size 每页条数
     * @param callback 回调接口
     */
    public void getAvailableGroups(int page, int size, final GroupCallback<List<GroupInfo>> callback) {
        apiService.getAvailableGroups(page, size).enqueue(new Callback<List<GroupInfo>>() {
            @Override
            public void onResponse(Call<List<GroupInfo>> call, Response<List<GroupInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("获取公开群组失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GroupInfo>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 加入群组
     * @param groupId 群组ID
     * @param invitationCode 邀请码(可为null)
     * @param callback 回调接口
     */
    public void joinGroup(String groupId, String invitationCode, final GroupCallback<GroupOperationResponse> callback) {
        JoinGroupRequest request = new JoinGroupRequest(invitationCode);
        apiService.joinGroup(groupId, request).enqueue(new Callback<GroupOperationResponse>() {
            @Override
            public void onResponse(Call<GroupOperationResponse> call, Response<GroupOperationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("加入群组失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GroupOperationResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 获取当前用户已加入的群组
     * @param callback 回调接口
     */
    public void getJoinedGroups(final GroupCallback<List<GroupInfo>> callback) {
        apiService.getJoinedGroups().enqueue(new Callback<List<GroupInfo>>() {
            @Override
            public void onResponse(Call<List<GroupInfo>> call, Response<List<GroupInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("获取已加入群组失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GroupInfo>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 退出群组
     * @param groupId 群组ID
     * @param callback 回调接口
     */
    public void leaveGroup(String groupId, final GroupCallback<GroupOperationResponse> callback) {
        apiService.leaveGroup(groupId).enqueue(new Callback<GroupOperationResponse>() {
            @Override
            public void onResponse(Call<GroupOperationResponse> call, Response<GroupOperationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("退出群组失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<GroupOperationResponse> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }

    /**
     * 上传群组照片
     * @param groupId 群组ID
     * @param imageUri 图片URI
     * @param context 上下文
     * @param callback 回调接口
     */
    public void uploadGroupPhoto(String groupId, Uri imageUri, Context context, final GroupCallback<UploadResponse> callback) {
        try {
            // 复用photoRepository中准备文件的逻辑
            MultipartBody.Part photoPart = photoRepository.prepareFilePart("photo", imageUri, context);
            apiService.uploadGroupPhoto(groupId, photoPart).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError("上传群组照片失败: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    callback.onError("网络错误: " + t.getMessage());
                }
            });
        } catch (Exception e) {
            callback.onError("准备文件失败: " + e.getMessage());
        }
    }

    /**
     * 获取群组照片列表
     * @param groupId 群组ID
     * @param callback 回调接口
     */
    public void getGroupPhotos(String groupId, final GroupCallback<List<String>> callback) {
        apiService.getGroupPhotos(groupId).enqueue(new Callback<List<String>>() {
            @Override
            public void onResponse(Call<List<String>> call, Response<List<String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("获取群组照片列表失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<String>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
}
