// PhotoViewModel.java
package com.example.pa.ui.photo;


import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.bumptech.glide.Glide;
import com.example.pa.data.Daos.PhotoDao;
import com.example.pa.data.DatabaseHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.example.pa.data.Daos.PhotoDao;

import com.example.pa.data.Daos.PhotoDao.Photo;

public class PhotoViewModel extends ViewModel {

    private PhotoDao photoDao; // 添加 PhotoDao 引用
    private int currentUserId = 1; // 假设当前用户ID，根据实际逻辑获取
    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
    private final MutableLiveData<List<Photo>> imageList = new MutableLiveData<>(new ArrayList<>());

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    public LiveData<List<Photo>> getImageList() {
        return imageList;
    }



    // 初始化时传入 Context 或 PhotoDao（需根据项目结构调整）
    public void initPhotoDao(Context context) {
        photoDao = new PhotoDao(context);
    }


    public void loadPhotosFromDatabase() {
        if (photoDao == null) return;
        List<Photo> photos = photoDao.getPhotosByUserAsList(currentUserId);
        imageList.setValue(photos);
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
            List<Photo> currentList = imageList.getValue();
            if (currentList != null) {
                imageList.setValue(new ArrayList<>(currentList));
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    // 提供对外更新图片列表的方法
    public void updateImageList(List<Photo> newImages) {
        imageList.setValue(newImages);
    }
}
