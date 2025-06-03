package com.example.pa.data.model.user;

public class LoginResponse {
    private final boolean success;
    private String token;
    private String message;
    private User user;

    public LoginResponse(boolean success, String token, String message, User user) {
        this.success = success;
        this.token = token;
        this.message = message;
        this.user = user;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getToken() {
        return token;
    }

    public String getMessage() {
        return message;
    }

    public User getUser() {
        return user;
    }
}
