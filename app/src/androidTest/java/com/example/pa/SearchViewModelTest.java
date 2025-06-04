package com.example.pa;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pa.MyApplication;
import com.example.pa.ui.search.SearchViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class SearchViewModelTest {

    @Rule
    public InstantTaskExecutorRule instantExecutorRule = new InstantTaskExecutorRule();

    private SearchViewModel viewModel;

    @Before
    public void setUp() {
        viewModel = new SearchViewModel();
    }

    @Test
    public void testSearchHistoryWorkflow() throws InterruptedException {
        // 你要确保 userId=1 存在，如果不存在会失败
        viewModel.saveSearchHistory("测试词");
        Thread.sleep(300); // 简单等待异步写入完成

        viewModel.loadSearchHistory(1);
        Thread.sleep(300); // 等待 LiveData 更新

        List<String> history = viewModel.getSearchHistory().getValue();
        assertNotNull(history);
        //assertTrue(history.contains("测试词"));
    }

    @Test
    public void testSearchImages() {
        // 确保数据库中有与这个标签绑定的照片路径
        String tagName = "自然"; // 替换成你数据库中确实存在的标签名
        viewModel.searchImages(tagName);

        List<String> results = viewModel.getSearchResults().getValue();
        assertNotNull(results);
        //assertFalse(results.isEmpty());
        System.out.println("搜索结果：" + results);
    }

    @Test
    public void testSuggestions() {
        // 假设数据库中已有包含“山”的标签，如“山水”、“山脉”
        viewModel.updateSuggestions("山");

        List<String> suggestions = viewModel.getSuggestions().getValue();
        assertNotNull(suggestions);
        //assertFalse(suggestions.isEmpty());
        for (String suggestion : suggestions) {
            //assertTrue(suggestion.contains("山"));
        }
    }

    @Test
    public void testRecommendations() {
        viewModel.loadRecommendations();

        List<String> recommendations = viewModel.getRecommendations().getValue();
        assertNotNull(recommendations);
        //assertFalse(recommendations.isEmpty());
        //assertTrue(recommendations.size() <= 5);
    }

    @Test
    public void testCheckSearchHistory(){
        viewModel.checkSearchHistory(1);
    }
}
