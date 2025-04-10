// PhotoViewModel.java
package com.example.pa.ui.photo;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.data.DatabaseHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.example.pa.data.Daos.PhotoDao;

import com.example.pa.data.Daos.PhotoDao.Photo;

public class PhotoViewModel extends ViewModel {

    private PhotoDao photoDao; // 添加 PhotoDao 引用
    private int currentUserId = 1; // 假设当前用户ID，根据实际逻辑获取
    // LiveData 用于持有图片列表，UI 层可以观察此数据的变化
    private final MutableLiveData<List<Photo>> imageList = new MutableLiveData<>(new ArrayList<>());

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

    // 提供对外更新图片列表的方法
    public void updateImageList(List<Photo> newImages) {
        imageList.setValue(newImages);
    }
}
