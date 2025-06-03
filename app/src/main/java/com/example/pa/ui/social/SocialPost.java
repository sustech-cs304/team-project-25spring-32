package com.example.pa.ui.social;

public class SocialPost {
    private String username;
    private String content;
    private String imageUrl;
    private int imageResId;
    private String groupName;
    private boolean isUrl;

    public SocialPost(String username, String content, int imageResId, String groupName) {
        this.username = username;
        this.content = content;
        this.imageResId = imageResId;
        this.groupName = groupName;
        this.isUrl = false;
    }

    public SocialPost(String username, String content, String imageUrl, String groupName) {
        this.username = username;
        this.content = content;
        this.imageUrl = imageUrl;
        this.groupName = groupName;
        this.isUrl = true;
    }

    public String getUsername() { return username; }
    public String getContent() { return content; }
    public int getImageResId() { return imageResId; }
    public String getImageUrl() { return imageUrl; }
    public String getGroupName() { return groupName; }
    public boolean isUrl() { return isUrl; }
}
