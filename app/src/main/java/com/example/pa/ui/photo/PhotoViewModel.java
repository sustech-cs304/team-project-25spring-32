// PhotoViewModel.java
package com.example.pa.ui.photo;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.AndroidViewModel;

import com.bumptech.glide.Glide;
import com.example.pa.data.Daos.PhotoDao;
import com.example.pa.data.FileRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.pa.data.model.Photo;
import com.example.pa.data.cloudRepository.PhotoRepository;
import com.example.pa.data.model.UploadResponse;

public class PhotoViewModel extends AndroidViewModel {
    private final PhotoRepository repository;
    private final MutableLiveData<List<Photo>> photos = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>(null);

    private PhotoDao photoDao; // 添加 PhotoDao 引用
    private FileRepository fileRepository;
    private int currentUserId = 1; // 假设当前用户ID，根据实际逻辑获取
    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
    private final MutableLiveData<List<Photo>> imageList = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Uri>> URiList = new MutableLiveData<>(new ArrayList<>());

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public LiveData<List<Uri>> getURiList() {
        return URiList;
    }

    public LiveData<List<Photo>> getImageList() {
        return imageList;
    }


    public PhotoViewModel(Application application) {
        super(application);
        repository = new PhotoRepository();
        // 初始化 PhotoDao
        photoDao = new PhotoDao(application);
        // 加载网络数据
        loadPhotos();
    }
    // Getter 方法
    public LiveData<List<Photo>> getPhotos() {
        return photos;
    }
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }
    public LiveData<String> getError() {
        return error;
    }

    /*
    ====== 网络数据相关方法 ======
    */
    // 使用标准回调加载照片
    public void loadPhotos() {
        isLoading.setValue(true);
        error.setValue(null);

        repository.getPhotos(new PhotoRepository.PhotoCallback<List<Photo>>() {
            @Override
            public void onSuccess(List<Photo> result) {
                photos.postValue(result);
                isLoading.postValue(false);
                // 同步到本地数据库
                syncPhotosToDatabase(result);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("加载图片失败: " + errorMessage);
                isLoading.postValue(false);
                // 加载失败时从本地数据库获取
                loadPhotosFromDatabase();
            }
        });
    }

    // 上传照片 (标准回调)
    public void uploadPhoto(Uri imageUri, Context context) {
        isLoading.setValue(true);
        error.setValue(null);

        repository.uploadPhoto(imageUri, context, new PhotoRepository.PhotoCallback<UploadResponse>() {
            @Override
            public void onSuccess(UploadResponse result) {
                // 上传成功后重新加载图片列表
                loadPhotos();
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("上传图片失败: " + errorMessage);
                isLoading.postValue(false);
            }
        });
    }


    // 删除照片 (标准回调)
    public void deletePhoto(String filename) {
        isLoading.setValue(true);
        error.setValue(null);

        repository.deletePhoto(filename, new PhotoRepository.PhotoCallback<Map<String, String>>() {
            @Override
            public void onSuccess(Map<String, String> result) {
                // 删除成功后重新加载图片列表
                loadPhotos();
                // 从本地数据库也删除该照片
                deletePhotoFromDatabase(filename);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("删除图片失败: " + errorMessage);
                isLoading.postValue(false);
            }
        });
    }


    // ====== 本地数据库相关方法 ======


    // 初始化时传入 Context 或 PhotoDao（需根据项目结构调整）
    public void initPhotoDao(Context context) {
        photoDao = new PhotoDao(context);
    }

    //在后台线程中执行数据库操作 通过Handler将结果发送回主线程 避免了UI阻塞问题
    public void loadPhotosFromDatabase() {
        if (photoDao == null) return;
        executorService.execute(() -> {
            List<Photo> localPhotos = photoDao.getPhotosByUserAsList(currentUserId);
            new Handler(Looper.getMainLooper()).post(() -> {
                imageList.setValue(localPhotos);
            });
        });
    }

    // 将网络照片同步到本地数据库
    private void syncPhotosToDatabase(List<Photo> networkPhotos) {
        if (networkPhotos == null) return;

        executorService.execute(() -> {
            for (Photo photo : networkPhotos) {
                // 检查是否已存在
                Photo existingPhoto = photoDao.getPhotoById(photo.id);
                if (existingPhoto == null) {
                    // 不存在则添加到数据库
                    photoDao.addFullPhoto(photo);
                }

                // 下载图片到本地文件系统
                if (photo.fileUrl != null) {
                    downloadAndSaveImage(getApplication(), photo);
                }
            }
        });
    }

    // 从数据库删除照片
    private void deletePhotoFromDatabase(String filename) {
        if (photoDao == null) return;
        executorService.execute(() -> {
            // 查找并删除匹配文件名的照片
            // 注意：这里可能需要根据具体数据库结构调整实现
            List<Photo> photos = photoDao.getPhotosByUserAsList(currentUserId);
            for (Photo photo : photos) {
                if (photo.filename != null && photo.filename.equals(filename)) {
                    photoDao.deletePhoto(photo.id);
                    break;
                }
            }
        });
    }

    // 初始化时传入 Context
    public void initFileRepository(Context context) {
        fileRepository = new FileRepository(context);
    }

    // 直接从文件系统里面读取 DCIM/Camera文件夹下的所有照片
    public void loadURIFromRepository() {
        List<Uri> uriList = fileRepository.getAlbumImages("所有照片");
        System.out.println(uriList);
        URiList.setValue(uriList);
    }

    private void downloadAndSaveImage(Context context, Photo photo) {
        executorService.execute(() -> {
            // 检查图片是否已存在
            File imageFile = new File(photo.filePath);
            if (imageFile.exists()) {
                Log.d("PhotoViewModel", "Image already exists: " + photo.filePath);
                return;
            }
            try {
                // 创建本地存储目录
                File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                String fileName = "photo_" + photo.id + ".jpg";
                imageFile = new File(storageDir, fileName);

                // 如果文件已存在，跳过下载
                if (imageFile.exists()) {
                    updatePhotoPath(photo, imageFile.getAbsolutePath());
                    return;
                }

                // 使用 Glide 下载图片
                Bitmap bitmap = Glide.with(context)
                        .asBitmap()
                        .load(photo.fileUrl)
                        .submit()
                        .get();

                // 保存到本地文件
                FileOutputStream fos = new FileOutputStream(imageFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
                fos.close();

                // 更新照片路径并存入数据库
                updatePhotoPath(photo, imageFile.getAbsolutePath());

                // 保存到数据库
                photoDao.addFullPhoto(photo);

                Log.d("PhotoViewModel", "Image saved: " + fileName);

            } catch (Exception e) {
                Log.e("PhotoViewModel", "Error saving image: " + e.getMessage());
            }
        });
    }

    private void updatePhotoPath(Photo photo, String localPath) {
        // 更新数据库中的本地路径
        // TODO: 添加 updatePhotoPath 方法到 PhotoDao
//        photoDao.updatePhotoPath(photo.id, localPath);

        // 更新内存中的对象
        photo.filePath = localPath;

        // 通知 UI 更新（需要在主线程执行）
        new Handler(Looper.getMainLooper()).post(() -> {
            List<Uri> currentList = URiList.getValue();
            if (currentList != null) {
                URiList.setValue(new ArrayList<>(currentList));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    // 提供对外更新图片列表的方法
    public void updateUriList(List<Uri> newURiList) {
        URiList.setValue(newURiList);
    }
}
