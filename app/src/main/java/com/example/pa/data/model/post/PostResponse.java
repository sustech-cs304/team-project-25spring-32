package com.example.pa.data.model.post;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PostResponse {
    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("post")
    private Post post;

    @SerializedName("posts")
    private List<Post> posts;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Post getPost() {
        return post;
    }

    public List<Post> getPosts() {
        return posts;
    }
} 