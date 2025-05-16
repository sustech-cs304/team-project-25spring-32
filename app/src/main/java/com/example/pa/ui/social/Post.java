package com.example.pa.ui.social;

public class Post {
    private int avatarResId;
    private String username;
    private String content;
    private int imageResId;

    public Post(int avatarResId, String username, String content, int imageResId) {
        this.avatarResId = avatarResId;
        this.username = username;
        this.content = content;
        this.imageResId = imageResId;
    }

    // Getter 方法
    public int getAvatarResId() { return avatarResId; }
    public String getUsername() { return username; }
    public String getContent() { return content; }
    public int getImageResId() { return imageResId; }
}