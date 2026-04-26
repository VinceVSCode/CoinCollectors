// v0.3.0: User model for coin collection ownership tracking.
package com.vincevscode.cointracker.model;

import java.util.Objects;

public class User {
    private int id;
    private String username;

    public User(int id, String username) {
        this.id = id;
        this.username = username;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
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

        User otherUser = (User) otherObject;

        return id == otherUser.id
                && Objects.equals(username, otherUser.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, username);
    }
}