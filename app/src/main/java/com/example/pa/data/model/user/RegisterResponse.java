// RegisterResponse.java
package com.example.pa.data.model.user;

import com.google.gson.annotations.SerializedName;

public class RegisterResponse {
    @SerializedName("success") // 使用 Gson 注解匹配 JSON 字段
    private boolean success;

    @SerializedName("token")
    private String token;

    @SerializedName("message")
    private String message;

    @SerializedName("user")
    private User user;

    public String getToken() { return token; }
    public User getUser() { return user; }
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}