// v0.4.3: View model for user screen/query results.
package com.vincevscode.cointracker.view;

import java.util.Objects;

public class UserView {
    private int userId;
    private String username;

    public UserView(int userId, String username) {
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
        return "UserView{" +
                "userId=" + userId +
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

        UserView otherView = (UserView) otherObject;
        return userId == otherView.userId && Objects.equals(username, otherView.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, username);
    }
}