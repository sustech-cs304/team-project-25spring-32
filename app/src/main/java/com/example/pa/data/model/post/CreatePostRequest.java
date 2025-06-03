package com.example.pa.data.model.post;

public class CreatePostRequest {
    private String[] imageUrls;

    public CreatePostRequest(String[] imageUrls) {
        this.imageUrls = imageUrls;
    }

    public String[] getImageUrls() {
        return imageUrls;
    }
} 