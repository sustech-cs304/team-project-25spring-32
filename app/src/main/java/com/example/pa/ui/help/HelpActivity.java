package com.example.pa.ui.help;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        WebView webView = findViewById(R.id.webview);

        String htmlContent = getString(R.string.help_content);

        webView.loadData(htmlContent, "text/html", "UTF-8");
    }
}