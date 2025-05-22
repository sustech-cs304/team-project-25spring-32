package com.example.pa.ui.memory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.data.Daos.MemoryVideoDao.MemoryVideo;

import java.util.ArrayList;
import java.util.List;

public class MemoryViewModel extends ViewModel {
    private final MutableLiveData<List<MemoryVideo>> memoryVideos = new MutableLiveData<>();

    public MemoryViewModel() {
        List<MemoryVideo> sampleList = new ArrayList<>();
        sampleList.add(new MemoryVideo(
                1, 1001, "毕业纪念册", "回忆", "2023/06/20", "", "" // 可加入封面URL字段
        ));
        sampleList.add(new MemoryVideo(
                2, 1001, "旅行日记", "自然", "2024/02/15", "", ""
        ));
        sampleList.add(new MemoryVideo(
                3, 1001, "家庭聚会", "温馨", "2024/12/01", "", ""
        ));
        memoryVideos.setValue(sampleList);
    }

    public LiveData<List<MemoryVideo>> getMemoryVideos() {
        return memoryVideos;
    }
}
