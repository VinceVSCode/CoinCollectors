// v0.6.0: Request DTO for self-service user registration.
package com.vincevscode.cointracker.api.dto;

// Deliberately has ONLY username+password fields — no role or active flag can be supplied
// here, which is what makes UserRegistrationService's "always USER, always active" behavior
// enforceable rather than just a convention a caller could bypass by adding extra JSON fields.
public class RegisterRequest {
    private String username;
    private String password;

    public RegisterRequest() {
    }

    public RegisterRequest(String username, String password) {
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
