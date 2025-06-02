package com.example.pa.ui.search;

import android.annotation.SuppressLint;
import android.database.Cursor;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.pa.MainActivity;
import com.example.pa.MyApplication;
import com.example.pa.data.Daos.PhotoDao;
import com.example.pa.data.Daos.PhotoTagDao;
import com.example.pa.data.Daos.SearchHistoryDao;
import com.example.pa.data.Daos.TagDao;
import com.example.pa.data.MainRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class SearchViewModel extends ViewModel {
    /**
     * AI-generated-content
     * tool: Deepseek
     * version: latest
     * usage: 生成所有方法，自己修改了loadRecommendations的内容，改变了searchImages的逻辑
     */

    private final MutableLiveData<List<String>> mSuggestions;
    private final MutableLiveData<List<String>> mSearchResults;
    private final MutableLiveData<List<String>> mRecommendations = new MutableLiveData<>();
    private final MutableLiveData<Boolean> hasSearchHistory = new MutableLiveData<>();
    private final MutableLiveData<List<String>> searchHistory = new MutableLiveData<>();
    private PhotoTagDao photoTagDao;
    private PhotoDao photoDao;
    private TagDao tagDao;
    private SearchHistoryDao searchHistoryDao;
    private MainRepository mainRepository;




    public SearchViewModel() {
        photoTagDao = MyApplication.getInstance().getPhotoTagDao();
        photoDao = MyApplication.getInstance().getPhotoDao();
        tagDao = MyApplication.getInstance().getTagDao();
        searchHistoryDao=MyApplication.getInstance().getSearchHistoryDao();
        mainRepository= MyApplication.getInstance().getMainRepository();

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

    public LiveData<Boolean> getHasSearchHistory() {
        return hasSearchHistory;
    }

    public void updateSuggestions(String query) {
        List<String> suggestions = new ArrayList<>();
        Cursor cursor=tagDao.getAllTags();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex("name"));
                if (tagName.contains(query)&& !tagName.equals(query)) suggestions.add(tagName);
            }
            cursor.close();
        }
        if (suggestions.size()> 5) {
            // 限制建议数量为5个
            suggestions = suggestions.subList(0, 5);
        }
        mSuggestions.setValue(suggestions);
    }
    public void checkSearchHistory(int userId) {
        boolean exists = searchHistoryDao.hasSearchHistory(userId);
        hasSearchHistory.setValue(exists);
    }

    public void searchImages(String query) {
        List<String> results = mainRepository.getPhotoPathByTagName(query);
        mSearchResults.setValue(Objects.requireNonNullElseGet(results, ArrayList::new));
    }
    public void loadRecommendations() {
        // 模拟从数据库获取推荐词
        List<String> recommendations = new ArrayList<>();
        Cursor cursor=tagDao.getNonAiRecognizedTags();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String tagName = cursor.getString(cursor.getColumnIndex("name"));
                recommendations.add(tagName);
            }
            cursor.close();
        }

        // 随机选择3-5个推荐词
        Collections.shuffle(recommendations);
        int count = 3 + new Random().nextInt(3); // 3-5个
        mRecommendations.setValue(recommendations.subList(0, Math.min(count, recommendations.size())));
    }

    public void saveSearchHistory(String query) {
        // 添加搜索历史
        searchHistoryDao.addSearchHistory(1,query);//TODO:这里暂时写id为1，后面要改

    }
    public LiveData<List<String>> getSearchHistory() {
        return searchHistory;
    }

    public void loadSearchHistory(int userId) {
        List<String> history;
        Cursor cursor=searchHistoryDao.getSearchHistoryByUser(userId,5); // Fetch from DAO
        if (cursor != null) {
            history = new ArrayList<>();
            while (cursor.moveToNext()) {
                @SuppressLint("Range") String query = cursor.getString(cursor.getColumnIndex("query"));
                history.add(query);
            }
            cursor.close();
        } else {
            history = null;
        }
        searchHistory.setValue(history != null ? history : new ArrayList<>());
    }
}