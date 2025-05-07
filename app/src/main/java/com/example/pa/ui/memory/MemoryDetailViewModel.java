// MemoryDetailViewModel.java
package com.example.pa.ui.memory;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.MyApplication;
import com.example.pa.data.FileRepository;

import java.util.List;

public class MemoryDetailViewModel extends ViewModel {

    private final MutableLiveData<List<Uri>> photoUris = new MutableLiveData<>();
    private final FileRepository fileRepository;

    public MemoryDetailViewModel() {
        // 初始化每个相册的默认图片
        this.fileRepository = new FileRepository(MyApplication.getInstance());
    }

    public LiveData<List<Uri>> getPhotoUris() {
        return photoUris;
    }

    public void loadPhotos(String memoryId) {
        // TODO: 根据memoryId从数据源加载真实数据
//        List<Uri> sampleUris = new ArrayList<>();
//        sampleUris.add(Uri.parse("content://media/external/images/media/1"));
//        sampleUris.add(Uri.parse("content://media/external/images/media/2"));
//        sampleUris.add(Uri.parse("content://media/external/images/media/3"));
        List<Uri> sampleUris = fileRepository.getAlbumImages("所有照片");
        photoUris.setValue(sampleUris);
    }

    public void updatePhotos(List<Uri> newUris) {
        photoUris.setValue(newUris);
    }

    public void handleAddPhotos() {
    }

    public void handleBatchDelete(List<Uri> selectedItems) {
        
    }

    public void handleExportVideo() {
    }
}