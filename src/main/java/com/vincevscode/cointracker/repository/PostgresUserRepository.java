// v0.3.0: PostgreSQL repository implementation for user storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.db.DatabaseConnection;
import com.vincevscode.cointracker.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresUserRepository implements UserRepositoryInterface {

    @Override
    public void addUser(User user) {
        String sql = """
                INSERT INTO users (id, username)
                VALUES (?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, user.getId());
            statement.setString(2, user.getUsername());

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to add user to PostgreSQL.", exception);
        }
    }

    @Override
    public List<User> getAllUsers() {
        String sql = """
                SELECT id, username
                FROM users
                ORDER BY id
                """;

        List<User> users = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String username = resultSet.getString("username");

                users.add(new User(id, username));
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to retrieve users from PostgreSQL.", exception);
        }

        return users;
    }

    @Override
    public User findUserById(int id) {
        String sql = """
                SELECT id, username
                FROM users
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int userId = resultSet.getInt("id");
                    String username = resultSet.getString("username");

                    return new User(userId, username);
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find user by ID in PostgreSQL.", exception);
        }

        return null;
    }
}