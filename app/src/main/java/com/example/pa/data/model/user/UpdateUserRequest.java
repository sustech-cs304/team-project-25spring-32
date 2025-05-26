// UpdateUserRequest.java
package com.example.pa.data.model.user;

public class UpdateUserRequest {
    private String username;
    private String email;

    public UpdateUserRequest(String username, String email) {
        this.username = username;
        this.email = email;
    }

    // Getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
}