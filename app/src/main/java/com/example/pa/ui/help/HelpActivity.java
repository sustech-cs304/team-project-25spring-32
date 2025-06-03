package com.example.pa.ui.help;

import android.os.Bundle;
import android.webkit.WebView;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pa.R;

public class HelpActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);
        Button button=findViewById(R.id.back_button);
        button.setOnClickListener(v -> finish());

        WebView webView = findViewById(R.id.webview);

        String htmlContent =
                "<html>" +
                "<head>" +
                "    <title>Smart Album Manual</title>" +

                "</head>" +
                "<body>" +
                "    <h1>Welcome to Smart Album!</h1>" +
                "    <p>This manual will help you quickly learn how to use its features to efficiently manage, edit, and share your photos.</p>" +
                "    <h2>1. Smart Album Management</h2>" +
                "    <p><span class=\"highlight\">New Album:</span> Tap \"+\" to create a custom-themed album (e.g., \"Family Gathering\") and manually add photos.</p>" +
                "    <h2>2. Quick Photo Search</h2>" +
                "    <p><span class=\"highlight\">Keyword Search:</span> Enter dates (e.g., \"2023,\" \"July\"), locations (e.g., \"Tokyo\"), tags (e.g., \"Beach\"), or objects (e.g., \"Cake\").</p>" +
                "    <p><span class=\"highlight\">Saved Search History:</span> Save past searches (e.g., \"Cat\") as shortcuts for quick access.</p>" +
                "    <p><span class=\"highlight\">Suggested Searches:</span> The system recommends keywords—tap to trigger an automatic search.</p>" +
                "    <p><span class=\"highlight\">Smart Autocomplete:</span> As you type, the system suggests keywords (e.g., typing \"Summer\" may recommend \"2023 Summer Trip\").</p>" +
                "    <h2>3. Smart Editing Tools</h2>" +
                "    <p><span class=\"highlight\">Basic Edits:</span> Rotate, crop, mirror flip, adjust brightness/contrast, or apply filters.</p>" +
                "    <p><span class=\"highlight\">AI Enhancements:</span> One-click background removal—automatically erase photo backgrounds, perfect for ID photos or creative designs.</p>" +
                "    <h2>4. Memory Videos</h2>" +
                "    <p><span class=\"highlight\">Auto-Generated Videos:</span> Select a theme (e.g., \"Graduation,\" \"2023 Travel\"), and the system curates highlights (e.g., \"Best Smiles,\" \"Sunset Moments\").</p>" +
                "    <p><span class=\"highlight\">Custom Adjustments:</span> Manually add/remove photos or clips. Add custom background music and transition effects.</p>" +
                "    <h2>5. Sharing & Social</h2>" +
                "    <p><span class=\"highlight\">Quick Sharing:</span> Share photos directly to in-app communities.</p>" +
                "    <p><span class=\"highlight\">Create Groups:</span> Start themed groups where everyone can share photos on the same topic.</p>" +
                "    <p style=\"margin-top: 30px; font-weight: bold;\">Start exploring Smart Album today and effortlessly manage your precious memories!</p>" +
                "</body>" +
                "</html>";

        webView.loadData(htmlContent, "text/html", "UTF-8");
    }
}