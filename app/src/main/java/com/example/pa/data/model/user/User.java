package com.example.pa.data.model.user;

public class User {
    public final String username;
    public final String email;
    public final String createdTime;
    public final String avatarPath;
    public final String avatarUrl;

    public User(int id, String username, String email,
                String createdTime, String avatarPath, String avatarUrl) {
        this.username = username;
        this.email = email;
        this.createdTime = createdTime;
        this.avatarPath = avatarPath;
        this.avatarUrl = avatarUrl;
    }
}
