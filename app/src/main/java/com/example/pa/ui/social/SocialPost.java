package com.example.pa.ui.social;

public class SocialPost {
    private String username;
    private String content;
    private int imageResId;

    public SocialPost(String username, String content, int imageResId) {
        this.username = username;
        this.content = content;
        this.imageResId = imageResId;
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public int getImageResId() { return imageResId; }
}
