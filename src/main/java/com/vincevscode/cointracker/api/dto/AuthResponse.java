// v0.6.0: Response DTO for authenticated user identity; never echoes password/hash.
package com.vincevscode.cointracker.api.dto;

import com.vincevscode.cointracker.model.UserRole;

public class AuthResponse {
    private int userId;
    private String username;
    private UserRole role;

    public AuthResponse(int userId, String username, UserRole role) {
        this.userId = userId;
        this.username = username;
        this.role = role;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public UserRole getRole() {
        return role;
    }
}
