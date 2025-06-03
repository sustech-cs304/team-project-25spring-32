package com.example.pa.data.cloudRepository;

import android.content.Context;
import android.net.Uri;

import com.example.pa.data.model.UploadResponse;
import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.network.GroupApiService;
import com.example.pa.data.network.RetrofitClient;

import java.io.File;
import java.util.List;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupRepository {
    private final GroupApiService apiService;
    private final PhotoRepository photoRepository;

    public GroupRepository() {
        this.apiService = RetrofitClient.getInstance().getGroupApiService();
        this.photoRepository = new PhotoRepository();
    }

    public interface GroupCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    // 创建群组
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


    // 上传群组照片，复用PhotoRepository的方法
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

    // 获取用户已加入的群组
    public void getJoinedGroups(final GroupCallback<List<GroupInfo>> callback) {
        apiService.getJoinedGroups().enqueue(new Callback<List<GroupInfo>>() {
            @Override
            public void onResponse(Call<List<GroupInfo>> call, Response<List<GroupInfo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("获取群组列表失败: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<GroupInfo>> call, Throwable t) {
                callback.onError("网络错误: " + t.getMessage());
            }
        });
    }
}