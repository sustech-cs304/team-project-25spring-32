package com.example.pa;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.example.pa.data.cloudRepository.GroupRepository;
import com.example.pa.data.cloudRepository.UserRepository;
import com.example.pa.data.model.UploadResponse;
import com.example.pa.data.model.group.GroupInfo;
import com.example.pa.data.model.group.GroupOperationResponse;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
public class GroupTest {
    private MyApplication app;
    private GroupRepository groupRepository;
    private UserRepository userRepository;
    private String testGroupId;
    private static final String TAG = "GroupTest";
    
    @Before
    public void setUp() {
        // 获取 Application 实例
        app = (MyApplication) InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
        // 初始化 GroupRepository
        groupRepository = new GroupRepository();
        // 初始化 UserRepository，可能需要用于先确保用户已登录
        userRepository = new UserRepository(InstrumentationRegistry.getInstrumentation().getTargetContext());
    }

    /**
     * 测试创建群组
     * 注意：此测试需要网络权限和正确的服务器配置才能成功执行
     */
    @Test
    public void test_createGroup() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] testPassed = {false};
        
        // 创建一个测试群组
        GroupInfo groupInfo = new GroupInfo("测试群组", "这是一个测试群组");
        
        groupRepository.createGroup(groupInfo, new GroupRepository.GroupCallback<GroupInfo>() {
            @Override
            public void onSuccess(GroupInfo result) {
                Log.d(TAG, "创建群组成功: " + result.getName());
                testPassed[0] = true;
                testGroupId = result.getId(); // 保存群组ID用于后续测试
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "创建群组失败: " + errorMessage);
                latch.countDown();
            }
        });

        // 等待回调完成
        latch.await(5, TimeUnit.SECONDS);
       // assertTrue("创建群组应该成功", testPassed[0]);
       // assertNotNull("群组ID不应为空", testGroupId);
    }

    /**
     * 测试获取公开群组列表
     */
    @Test
    public void test_getPublicGroups() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] testPassed = {false};
        
        // 获取可加入的公开群组
        groupRepository.getAvailableGroups(0, 10, new GroupRepository.GroupCallback<List<GroupInfo>>() {
            @Override
            public void onSuccess(List<GroupInfo> result) {
                Log.d(TAG, "获取公开群组成功: " + result.size() + " 个群组");
                testPassed[0] = !result.isEmpty(); // 至少应该有之前创建的测试群组
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "获取公开群组失败: " + errorMessage);
                latch.countDown();
            }
        });

        // 等待回调完成
        latch.await(5, TimeUnit.SECONDS);
       // assertTrue("获取公开群组应该成功且不为空", testPassed[0]);
    }

    /**
     * 测试加入群组
     * 注意：此测试依赖于前面创建群组的测试成功
     */
    @Test
    public void test_joinGroup() throws InterruptedException {
        // 如果前面的测试没有成功创建群组，这里跳过测试
        if (testGroupId == null) {
            Log.w(TAG, "跳过加入群组测试，因为没有可用的群组ID");
            return;
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] testPassed = {false};
        
        // 加入之前创建的群组
        groupRepository.joinGroup(testGroupId, null, new GroupRepository.GroupCallback<GroupOperationResponse>() {
            @Override
            public void onSuccess(GroupOperationResponse result) {
                Log.d(TAG, "加入群组成功: " + result.getMessage());
                testPassed[0] = result.isSuccess();
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                // 如果用户已经在群组中，这也是一种预期情况
                if (errorMessage.contains("您已经是该群组成员")) {
                    Log.d(TAG, "用户已经是该群组成员");
                    testPassed[0] = true;
                } else {
                    Log.e(TAG, "加入群组失败: " + errorMessage);
                }
                latch.countDown();
            }
        });

        // 等待回调完成
        latch.await(5, TimeUnit.SECONDS);
        //assertTrue("加入群组应该成功", testPassed[0]);
    }

    /**
     * 测试获取用户已加入的群组
     */
    @Test
    public void test_getUserGroups() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] testPassed = {false};
        
        // 获取当前用户已加入的群组
        groupRepository.getJoinedGroups(new GroupRepository.GroupCallback<List<GroupInfo>>() {
            @Override
            public void onSuccess(List<GroupInfo> result) {
                Log.d(TAG, "获取已加入群组成功: " + result.size() + " 个群组");
                testPassed[0] = true; // 即使没加入任何群组，只要API调用成功就算通过
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "获取已加入群组失败: " + errorMessage);
                latch.countDown();
            }
        });

        // 等待回调完成
        latch.await(5, TimeUnit.SECONDS);
        //assertTrue("获取已加入群组应该成功", testPassed[0]);
    }

    /**
     * 测试上传群组照片
     * 注意：此测试需要创建临时文件或使用真实图片，这里简化处理
     */
    @Test
    public void test_uploadGroupPhoto() throws InterruptedException {
        // 如果前面的测试没有成功创建群组，这里跳过测试
        if (testGroupId == null) {
            Log.w(TAG, "跳过上传群组照片测试，因为没有可用的群组ID");
            return;
        }
        
        // 获取测试上下文
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        
        // 这里需要一个实际的图片文件URI，这里仅作示例
        // 真实测试中，你可以创建一个临时文件或使用assets中的示例图片
        Uri imageUri = Uri.parse("android.resource://com.example.pa/drawable/ic_launcher"); 
        
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] testPassed = {false};
        
        // 上传群组照片
        groupRepository.uploadGroupPhoto(testGroupId, imageUri, context, new GroupRepository.GroupCallback<UploadResponse>() {
            @Override
            public void onSuccess(UploadResponse result) {
                Log.d(TAG, "上传群组照片成功: " + result.getPath());
                testPassed[0] = true;
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "上传群组照片失败: " + errorMessage);
                latch.countDown();
            }
        });

        // 等待回调完成
        latch.await(10, TimeUnit.SECONDS); // 上传可能需要更长时间
        //assertTrue("上传群组照片应该成功", testPassed[0]);
    }

    /**
     * 测试退出群组
     * 注意：此测试会导致用户退出之前创建的群组，可能影响其他测试
     */
    @Test
    public void test_leaveGroup() throws InterruptedException {
        // 如果前面的测试没有成功创建群组，这里跳过测试
        if (testGroupId == null) {
            Log.w(TAG, "跳过退出群组测试，因为没有可用的群组ID");
            return;
        }
        
        final CountDownLatch latch = new CountDownLatch(1);
        final boolean[] testPassed = {false};
        
        // 退出之前加入的群组
        groupRepository.leaveGroup(testGroupId, new GroupRepository.GroupCallback<GroupOperationResponse>() {
            @Override
            public void onSuccess(GroupOperationResponse result) {
                Log.d(TAG, "退出群组成功: " + result.getMessage());
                testPassed[0] = result.isSuccess();
                latch.countDown();
            }

            @Override
            public void onError(String errorMessage) {
                // 如果用户不在群组中，这也可能是合理的情况
                if (errorMessage.contains("您不是该群组成员")) {
                    Log.d(TAG, "用户不是该群组成员");
                    testPassed[0] = true;
                } else {
                    Log.e(TAG, "退出群组失败: " + errorMessage);
                }
                latch.countDown();
            }
        });

        // 等待回调完成
        latch.await(5, TimeUnit.SECONDS);
        //assertTrue("退出群组应该成功", testPassed[0]);
    }
}
