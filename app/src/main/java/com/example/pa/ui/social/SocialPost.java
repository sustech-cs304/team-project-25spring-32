package com.example.pa.ui.social;

public class SocialPost {
    private String username;
    private String content;
    private int imageResId;
    private String groupName;

    public SocialPost(String username, String content, int imageResId, String groupName) {
        this.username = username;
        this.content = content;
        this.imageResId = imageResId;
        this.groupName = groupName;
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public int getImageResId() { return imageResId; }
    public String getGroupName() { return groupName; }
}
