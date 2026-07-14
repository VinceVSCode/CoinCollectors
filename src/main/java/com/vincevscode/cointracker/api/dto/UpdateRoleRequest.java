// v0.7.1: Request DTO for changing a user's role (admin operation).
package com.vincevscode.cointracker.api.dto;

import com.vincevscode.cointracker.model.UserRole;

public class UpdateRoleRequest {
    // Left null if omitted from the request body (Jackson doesn't invent an enum value) —
    // AdminUserController checks for that and rejects with a clear 400 rather than a NPE.
    private UserRole role;

    public UpdateRoleRequest() {
    }

    public UpdateRoleRequest(UserRole role) {
        this.role = role;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }
}
