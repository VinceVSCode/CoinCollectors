// v0.10.0: Identifies the admin performing an audited action.
package com.vincevscode.cointracker.model;

import java.util.Objects;

/**
 * The "who" of an audit record, passed from the controller down into the services that perform
 * privileged mutations.
 *
 * <p>Passing this explicitly rather than having services read {@code SecurityContextHolder}
 * keeps the service layer free of Spring Security (the same separation noted on
 * {@link com.vincevscode.cointracker.service.CollectionTrackingService}: services validate data,
 * controllers own authorization) and makes it impossible to call an audited operation without
 * saying who is responsible for it.
 */
public class AdminActor {
    private final int userId;
    private final String username;

    public AdminActor(int userId, String username) {
        if (userId <= 0) {
            throw new IllegalArgumentException("Actor user ID must be greater than 0.");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Actor username is required.");
        }

        this.userId = userId;
        this.username = username;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "AdminActor{userId=" + userId + ", username='" + username + "'}";
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        AdminActor otherActor = (AdminActor) otherObject;

        return userId == otherActor.userId && Objects.equals(username, otherActor.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}
