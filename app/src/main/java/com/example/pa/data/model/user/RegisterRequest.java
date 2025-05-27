// RegisterRequest.java
package com.example.pa.data.model.user;

public class RegisterRequest {
    private String username;
    private String email;
    private String password;

    public RegisterRequest(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // Getters
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
}