 package com.example.pa;

 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;

 import android.content.Context;
 import android.database.Cursor;
 import android.util.Log;
 import android.view.View;
 import android.widget.TextView;
 import android.widget.ImageView;

 import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
 import androidx.lifecycle.Observer;

 import com.example.pa.data.cloudRepository.GroupRepository;
 import com.example.pa.data.model.group.GroupInfo;
 import com.example.pa.ui.social.SocialPost;
 import com.example.pa.ui.social.SocialPostAdapter;

 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.Mock;
 import org.mockito.MockitoAnnotations;
 import org.robolectric.RobolectricTestRunner;
 import org.robolectric.annotation.Config;
 import org.robolectric.shadows.ShadowLog;

 import java.util.ArrayList;
 import java.util.List;

 @RunWith(RobolectricTestRunner.class)
 @Config(sdk = 28)
 public class SocialTest {

     @Rule
     public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

     @Mock
     private Context mockContext;

     @Mock
     private GroupRepository groupRepository;

     @Mock
     private View mockView;

     @Mock
     private TextView mockUsernameText;

     @Mock
     private TextView mockContentText;

     @Mock
     private ImageView mockPostImage;

     private List<SocialPost> postList;
     private SocialPostAdapter adapter;

     @Before
     public void setUp() {
         MockitoAnnotations.openMocks(this);
         // 设置 Robolectric 的 Log 模拟
         ShadowLog.stream = System.out;

         postList = new ArrayList<>();
         adapter = new SocialPostAdapter(postList);

         // 设置模拟视图的findViewById行为
         when(mockView.findViewById(R.id.textUsername)).thenReturn(mockUsernameText);
         when(mockView.findViewById(R.id.textContent)).thenReturn(mockContentText);
         when(mockView.findViewById(R.id.imagePost)).thenReturn(mockPostImage);
     }

     @Test
     public void testCreateSocialPost() {
         // 测试创建带有本地图片资源的帖子
         SocialPost post1 = new SocialPost("testUser", "测试内容", R.drawable.sample_image, "测试群组");
         assertEquals("testUser", post1.getUsername());
         assertEquals("测试内容", post1.getContent());
         assertEquals(R.drawable.sample_image, post1.getImageResId());
         assertEquals("测试群组", post1.getGroupName());
         assertFalse(post1.isUrl());

         // 测试创建带有URL的帖子
         SocialPost post2 = new SocialPost("testUser", "测试内容", "http://example.com/image.jpg", "测试群组");
         assertEquals("testUser", post2.getUsername());
         assertEquals("测试内容", post2.getContent());
         assertEquals("http://example.com/image.jpg", post2.getImageUrl());
         assertEquals("测试群组", post2.getGroupName());
         assertTrue(post2.isUrl());
     }

//     @Test
//     public void testSocialPostAdapter() {
//         // 准备测试数据
//         postList.add(new SocialPost("user1", "内容1", R.drawable.sample_image, "群组1"));
//         postList.add(new SocialPost("user2", "内容2", "http://example.com/image.jpg", "群组2"));
//
//         // 测试适配器项目数量
//         assertEquals(2, adapter.getItemCount());
//
//         // 创建ViewHolder
//         SocialPostAdapter.ViewHolder holder = new SocialPostAdapter.ViewHolder(mockView);
//
//         // 测试数据绑定
//         adapter.onBindViewHolder(holder, 0);
//
//         // 验证视图更新
//         verify(mockUsernameText).setText("user1");
//         verify(mockContentText).setText("内容1");
//     }
//
//     @Test
//     public void testGroupRepository() {
//         // 模拟群组数据
//         List<GroupInfo> mockGroups = new ArrayList<>();
//         GroupInfo group1 = new GroupInfo();
//         group1.setId("1");
//         group1.setName("测试群组1");
//         mockGroups.add(group1);
//
//         // 模拟回调
//         GroupRepository.GroupCallback<List<GroupInfo>> callback = mock(GroupRepository.GroupCallback.class);
//
//         // 测试获取群组列表
//         groupRepository.getAvailableGroups(0, 10, callback);
//         verify(callback).onSuccess(mockGroups);
//     }
 }
