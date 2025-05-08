// MemoryDetailActivity.java
package com.example.pa.ui.memory;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.pa.R;

public class MemoryDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_memory_detail);

        // 获取传递的记忆相册ID
        String memoryId = getIntent().getStringExtra("memory_id");

        // 加载Fragment
        if (savedInstanceState == null) {
            Fragment fragment = MemoryDetailFragment.newInstance(memoryId);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

    }
}