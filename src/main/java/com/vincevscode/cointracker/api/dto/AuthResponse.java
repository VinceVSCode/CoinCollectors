// v0.6.0: Response DTO for authenticated user identity; never echoes password/hash.
package com.vincevscode.cointracker.api.dto;

import com.vincevscode.cointracker.model.UserRole;

// Returned by register/login/me — carries only id+username+role, never the password hash,
// so it's safe to send straight from AuthController regardless of source (AuthUser or
// AuthUserDetails both happen to expose the same three fields it needs).
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
