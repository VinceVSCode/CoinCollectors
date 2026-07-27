// v0.4.0: Internal auth-focused user model for future authentication and authorization flows.
package com.vincevscode.cointracker.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * The authentication/authorization view of a user row: credentials, role, and account status.
 * Adapted to Spring Security via {@link com.vincevscode.cointracker.security.AuthUserDetails}
 * (never exposed directly as a {@code UserDetails}, keeping Spring Security types out of the
 * domain model). {@code active=false} is how {@link com.vincevscode.cointracker.security.AccountStatusFilter}
 * enforces "deactivation takes effect on the very next request" rather than waiting for session expiry.
 */
public class AuthUser {
    private int id;
    private String username;
    private String passwordHash;
    private UserRole role;
    private boolean active;
    private LocalDateTime createdAt;

    public AuthUser(
            int id,
            String username,
            String passwordHash,
            UserRole role,
            boolean active,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
        this.createdAt = createdAt;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public UserRole getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "AuthUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", role=" + role +
                ", active=" + active +
                ", createdAt=" + createdAt +
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

        AuthUser otherUser = (AuthUser) otherObject;

        return id == otherUser.id
                && active == otherUser.active
                && Objects.equals(username, otherUser.username)
                && Objects.equals(passwordHash, otherUser.passwordHash)
                && role == otherUser.role
                && Objects.equals(createdAt, otherUser.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username, passwordHash, role, active, createdAt);
    }
}