package com.example.pa.ui.search;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<String>> mSuggestions;
    private final MutableLiveData<List<String>> mSearchResults;

    public SearchViewModel() {
        mSuggestions = new MutableLiveData<>();
        mSuggestions.setValue(new ArrayList<>());

        mSearchResults = new MutableLiveData<>();
        mSearchResults.setValue(new ArrayList<>());
    }

    public LiveData<List<String>> getSuggestions() {
        return mSuggestions;
    }

    public LiveData<List<String>> getSearchResults() {
        return mSearchResults;
    }

    public void updateSuggestions(String query) {
        List<String> newSuggestions = new ArrayList<>();
        if (query.length() > 0) {
            // 模拟一些推荐词
            newSuggestions.add(query + "1");
            newSuggestions.add(query + "2");
            newSuggestions.add(query + "3");
        }
        mSuggestions.setValue(newSuggestions);
    }

    public void searchImages(String query) {
        List<String> results = new ArrayList<>();
        if (query.length() > 0) {
            // 模拟一些搜索结果图片
            results.add("https://example.com/image1.jpg");
            results.add("https://example.com/image2.jpg");
            results.add("https://example.com/image3.jpg");
        }
        mSearchResults.setValue(results);
    }
}