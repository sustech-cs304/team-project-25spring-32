package com.example.pa.memory;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.pa.data.Daos.MemoryVideoDao.MemoryVideo;
import com.example.pa.ui.memory.MemoryViewModel;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.verify;

public class MemoryViewModelTest {

    // 这是一个 JUnit Rule，用于在测试 LiveData 时，确保所有 LiveData 相关的操作都在同一个线程上执行
    // 这样可以避免在测试中因为异步操作而导致的竞态条件问题。
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private MemoryViewModel memoryViewModel;

    @Mock
    private Observer<List<MemoryVideo>> mockObserver; // Mock 一个 LiveData 观察者

    @Before
    public void setUp() {
        // 初始化 Mockito 注解
        MockitoAnnotations.openMocks(this);
        // 创建 ViewModel 实例
        memoryViewModel = new MemoryViewModel();
    }

    @Test
    public void testMemoryViewModelInitializesWithSampleData() {
        // 获取 LiveData
        LiveData<List<MemoryVideo>> liveData = memoryViewModel.getMemoryVideos();

        // 观察 LiveData，以获取其当前值
        liveData.observeForever(mockObserver);

        // 验证 LiveData 不为空
        assertNotNull(liveData.getValue());
        // 验证 LiveData 包含 3 条数据
        assertEquals(3, liveData.getValue().size());

        // 验证第一条数据的标题
        assertEquals("Graduation", liveData.getValue().get(0).name);
        // 验证第二条数据的日期
        assertEquals("2024/02/15", liveData.getValue().get(1).createdTime);
        // 验证第三条数据的标签
        assertEquals("Warmth", liveData.getValue().get(2).theme);

        // 移除观察者，防止内存泄漏（虽然测试结束后通常会清理）
        liveData.removeObserver(mockObserver);
    }
}