package com.example.pa.data.cloudRepository;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import io.reactivex.rxjava3.core.Observable;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.example.pa.data.model.Photo;
import com.example.pa.data.model.UploadResponse;
import com.example.pa.data.network.PhotoApiService;
import com.example.pa.data.network.RetrofitClient;

// 创建仓库类来处理网络请求
// PhotoRepository.java
public class PhotoRepository {
    private final PhotoApiService apiService;

    public PhotoRepository() {
        this.apiService = RetrofitClient.getInstance().getPhotoApiService();
    }

    public interface PhotoCallback<T> {
        void onSuccess(T result);
        void onError(String errorMessage);
    }

    // 使用标准Retrofit回调方式获取照片
    public void getPhotos(final PhotoCallback<List<Photo>> callback) {
        apiService.getPhotos().enqueue(new Callback<List<Photo>>() {
            @Override
            public void onResponse(Call<List<Photo>> call, Response<List<Photo>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to load photos: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Photo>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // 使用RxJava方式获取照片
    public Observable<List<Photo>> getPhotosRx() {
        return apiService.getPhotosRx();
    }

    // 上传照片 (标准回调)
    public void uploadPhoto(Uri imageUri, Context context, final PhotoCallback<UploadResponse> callback) {
        try {
            MultipartBody.Part photoPart = prepareFilePart("photo", imageUri, context);
            apiService.uploadPhoto(photoPart).enqueue(new Callback<UploadResponse>() {
                @Override
                public void onResponse(Call<UploadResponse> call, Response<UploadResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        callback.onSuccess(response.body());
                    } else {
                        callback.onError("Failed to upload photo: " + response.code());
                    }
                }

                @Override
                public void onFailure(Call<UploadResponse> call, Throwable t) {
                    callback.onError("Network error: " + t.getMessage());
                }
            });
        } catch (IOException e) {
            callback.onError("Error preparing file: " + e.getMessage());
        }
    }

    // 上传照片 (RxJava)
    public Observable<UploadResponse> uploadPhotoRx(Uri imageUri, Context context) {
        try {
            MultipartBody.Part photoPart = prepareFilePart("photo", imageUri, context);
            return apiService.uploadPhotoRx(photoPart);
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    // 删除照片 (标准回调)
    public void deletePhoto(String filename, final PhotoCallback<Map<String, String>> callback) {
        apiService.deletePhoto(filename).enqueue(new Callback<Map<String, String>>() {
            @Override
            public void onResponse(Call<Map<String, String>> call, Response<Map<String, String>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Failed to delete photo: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Map<String, String>> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    // 删除照片 (RxJava)
    public Observable<Map<String, String>> deletePhotoRx(String filename) {
        return apiService.deletePhotoRx(filename);
    }

    // 工具方法: 准备文件上传
    private MultipartBody.Part prepareFilePart(String partName, Uri fileUri, Context context) throws IOException {
        String mimeType = context.getContentResolver().getType(fileUri);
        if (mimeType == null) {
            mimeType = "image/jpeg";
        }

        File file = createTempFileFromUri(context, fileUri);
        RequestBody requestFile = RequestBody.create(MediaType.parse(mimeType), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    // 工具方法: 从Uri创建临时文件
    private File createTempFileFromUri(Context context, Uri uri) throws IOException {
        String fileName = getFileName(context, uri);
        File file = new File(context.getCacheDir(), fileName);

        InputStream inputStream = context.getContentResolver().openInputStream(uri);
        if (inputStream == null) {
            throw new IOException("Cannot open input stream for URI: " + uri);
        }

        OutputStream outputStream = new FileOutputStream(file);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        return file;
    }

    // 工具方法: 从Uri获取文件名
    private String getFileName(Context context, Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (columnIndex >= 0) {
                        result = cursor.getString(columnIndex);
                    }
                }
            }
        }

        if (result == null) {
            String path = uri.getPath();
            if (path != null) {
                int cut = path.lastIndexOf('/');
                if (cut != -1) {
                    result = path.substring(cut + 1);
                } else {
                    result = path;
                }
            }
        }

        return (result != null) ? result : "photo_" + System.currentTimeMillis() + ".jpg";
    }
}