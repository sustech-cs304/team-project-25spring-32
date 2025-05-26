package com.example.pa.ui.help;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;

public class HelpActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        TextView helpText = findViewById(R.id.help_text);
        Button backButton = findViewById(R.id.back_button);

        // 设置帮助文本内容
        String helpContent = getString(R.string.help_content);
        helpText.setText(helpContent);

        // 返回按钮点击事件
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 结束当前Activity，返回上一个Activity
            }
        });
    }
}