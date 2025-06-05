package com.example.pa.ui.select;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pa.MyApplication;
import com.example.pa.data.FileRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PhotoSelectViewModel extends AndroidViewModel {
    private final FileRepository repository;
    private final MutableLiveData<List<Uri>> photos = new MutableLiveData<>();
    private final MutableLiveData<Integer> selectedCount = new MutableLiveData<>(0);
    private final Set<Uri> selectedUris = new HashSet<>();

    public PhotoSelectViewModel(@NonNull Application application) {
        super(application);
        repository = MyApplication.getInstance().getFileRepository();
    }

    public void loadPhotos(String albumName) {
        List<Uri> existPhotos = repository.getAlbumImages(albumName);
        photos.postValue(existPhotos);
    }

    public void toggleSelection(Uri uri) {
        if (selectedUris.contains(uri)) {
            selectedUris.remove(uri);
        } else {
            selectedUris.add(uri);
        }
        selectedCount.postValue(selectedUris.size());
    }

    public LiveData<List<Uri>> getPhotos() {
        return photos;
    }

    public void updateSelectionCount(int count) {
        selectedCount.postValue(count);
    }

    public LiveData<Integer> getSelectedCount() {
        return selectedCount;
    }

    public List<Uri> getSelectedUris() {
        return new ArrayList<>(selectedUris);
    }
}