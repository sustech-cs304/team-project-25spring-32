package com.example.pa.ui.album;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AlbumViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public AlbumViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is album fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}