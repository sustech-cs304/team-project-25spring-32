package com.example.pa.ui.photo;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PhotoViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public PhotoViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is photo fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}