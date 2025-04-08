package com.example.pa.ui.search;

import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.MainActivity;
import com.example.pa.MyApplication;
import com.example.pa.data.Daos.PhotoDao;
import com.example.pa.data.Daos.PhotoTagDao;
import com.example.pa.data.Daos.TagDao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<List<String>> mSuggestions;
    private final MutableLiveData<List<String>> mSearchResults;
    private final MutableLiveData<List<String>> mRecommendations = new MutableLiveData<>();
    private PhotoTagDao photoTagDao;
    private PhotoDao photoDao;
    private TagDao tagDao;




    public SearchViewModel() {
        photoTagDao = MyApplication.getInstance().getPhotoTagDao();
        photoDao = MyApplication.getInstance().getPhotoDao();
        tagDao = MyApplication.getInstance().getTagDao();

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

    public LiveData<List<String>> getRecommendations() {
        return mRecommendations;
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
        Cursor cursor=tagDao.getTagByName(query);
        



        mSearchResults.setValue(results);
    }
    public void loadRecommendations() {
        // 模拟从数据库获取推荐词
        List<String> recommendations = new ArrayList<>();
        recommendations.add("Nature");
        recommendations.add("Animals");
        recommendations.add("Travel");
        recommendations.add("Food");
        recommendations.add("Technology");
        recommendations.add("Sports");
        recommendations.add("Art");

        // 随机选择3-5个推荐词
        Collections.shuffle(recommendations);
        int count = 3 + new Random().nextInt(3); // 3-5个
        mRecommendations.setValue(recommendations.subList(0, Math.min(count, recommendations.size())));
    }
}