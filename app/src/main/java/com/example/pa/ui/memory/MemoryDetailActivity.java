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
        String memoryName = getIntent().getStringExtra("memory_name");

        // 加载Fragment
        if (savedInstanceState == null) {
            Fragment fragment = MemoryDetailFragment.newInstance(memoryName);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, fragment)
                    .commit();
        }

    }
}