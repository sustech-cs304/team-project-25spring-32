package com.example.pa.ui.group.myGroup;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pa.R;

import java.util.ArrayList;
import java.util.List;

public class MyGroupActivity extends AppCompatActivity {

    private RecyclerView groupRecyclerView;
    private ProgressBar progressBar;
    private GroupAdapter groupAdapter;
    private List<Group> groupList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        groupRecyclerView = findViewById(R.id.groupRecyclerView);
        progressBar = findViewById(R.id.progressBar);

        // 设置布局管理器
        groupRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 初始化适配器
        groupAdapter = new GroupAdapter(groupList);
        groupRecyclerView.setAdapter(groupAdapter);

        // 加载群组数据
        loadGroups();
    }

    private void loadGroups() {
        progressBar.setVisibility(View.VISIBLE);

        // 这里应该是从网络或数据库获取群组列表
        // 模拟数据加载

    }

    // 模拟数据源方法 - 实际项目中替换为真实数据获取逻辑
    private List<Group> getGroupsFromDataSource() {
        List<Group> groups = new ArrayList<>();
        groups.add(new Group("1", "Android开发者", "讨论Android开发技术", 125, R.drawable.icon_social));
        groups.add(new Group("2", "产品经理交流", "产品设计与管理", 89, R.drawable.icon_social));
        groups.add(new Group("3", "UI/UX设计", "设计交流与分享", 76, R.drawable.icon_social));
        return groups;
    }

    // 群组数据模型
    public static class Group {
        private String id;
        private String name;
        private String description;
        private int memberCount;
        private int iconResId;

        public Group(String id, String name, String description, int memberCount, int iconResId) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.memberCount = memberCount;
            this.iconResId = iconResId;
        }

        // Getter方法...
    }

    // 适配器
    private class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> {

        private List<Group> groups;

        public GroupAdapter(List<Group> groups) {
            this.groups = groups;
        }

        @NonNull
        @Override
        public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_group, parent, false);
            return new GroupViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
            Group group = groups.get(position);
//            holder.groupName.setText(group.getName());
//            holder.groupDescription.setText(group.getDescription());
//            holder.memberCount.setText(group.getMemberCount() + "名成员");
//            holder.groupIcon.setImageResource(group.getIconResId());

//            holder.itemView.setOnClickListener(v -> {
//                // 处理点击事件，打开群组详情等
//                Toast.makeText(MyGroupActivity.this,
//                        "点击了: " + group.getName(), Toast.LENGTH_SHORT).show();
//            });
        }

        @Override
        public int getItemCount() {
            return groups.size();
        }

        class GroupViewHolder extends RecyclerView.ViewHolder {
            ImageView groupIcon;
            TextView groupName;
            TextView groupDescription;
            TextView memberCount;

            public GroupViewHolder(@NonNull View itemView) {
                super(itemView);
                groupIcon = itemView.findViewById(androidx.core.R.id.icon_group);
                groupName = itemView.findViewById(R.id.groupName);
                groupDescription = itemView.findViewById(R.id.groupDescription);
                memberCount = itemView.findViewById(R.id.memberCount);
            }
        }
    }
}