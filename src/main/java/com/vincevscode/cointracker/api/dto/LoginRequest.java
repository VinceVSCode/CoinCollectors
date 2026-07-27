// v0.6.0: Request DTO for username/password login.
package com.vincevscode.cointracker.api.dto;

// Mutable with a no-arg constructor + setters (unlike the immutable domain models in model/)
// because Jackson needs both to deserialize an incoming @RequestBody JSON object into this type.
public class LoginRequest {
    private String username;
    private String password;

    public LoginRequest() {
    }

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
