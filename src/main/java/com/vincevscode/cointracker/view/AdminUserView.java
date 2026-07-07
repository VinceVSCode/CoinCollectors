// v0.7.1: View model exposing user account fields for admin management screens.
package com.vincevscode.cointracker.view;

import com.vincevscode.cointracker.model.UserRole;

import java.util.Objects;

public class AdminUserView {
    private int userId;
    private String username;
    private UserRole role;
    private boolean active;

    public AdminUserView(int userId, String username, UserRole role, boolean active) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.active = active;
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

    public boolean isActive() {
        return active;
    }

    @Override
    public String toString() {
        return "AdminUserView{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", active=" + active +
                '}';
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        AdminUserView otherView = (AdminUserView) otherObject;

        return userId == otherView.userId
                && active == otherView.active
                && Objects.equals(username, otherView.username)
                && role == otherView.role;
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username, role, active);
    }
}
