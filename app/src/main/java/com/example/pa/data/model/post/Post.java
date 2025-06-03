package com.example.pa.data.model.post;

public class Post {
    private final int id;
    private final String[] imageUrls;
    private final String createdAt;

    public Post(int id, String[] imageUrls, String createdAt) {
        this.id = id;
        this.imageUrls = imageUrls;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }

    public String getCreatedAt() {
        return createdAt;
    }
} 