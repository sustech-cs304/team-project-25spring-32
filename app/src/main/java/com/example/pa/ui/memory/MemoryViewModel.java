package com.example.pa.ui.memory;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class MemoryViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    public MemoryViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is Memory fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}