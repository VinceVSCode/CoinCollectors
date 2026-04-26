// v0.3.0: Integration tests for PostgreSQL-backed user repository behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.db.DatabaseConnection;
import com.vincevscode.cointracker.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PostgresUserRepositoryTest {
    private PostgresUserRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PostgresUserRepository();
        clearUsersTable();
    }

    @Test
    void addUser_shouldStoreUserInDatabase() {
        User user = new User(1, "vince");

        repository.addUser(user);

        User foundUser = repository.findUserById(1);

        assertEquals(user, foundUser);
    }

    @Test
    void getAllUsers_shouldReturnAllStoredUsers() {
        repository.addUser(new User(1, "vince"));
        repository.addUser(new User(2, "alex"));

        List<User> users = repository.getAllUsers();

        assertEquals(2, users.size());
        assertEquals(new User(1, "vince"), users.get(0));
        assertEquals(new User(2, "alex"), users.get(1));
    }

    @Test
    void findUserById_shouldReturnUserWhenIdExists() {
        repository.addUser(new User(1, "vince"));

        User foundUser = repository.findUserById(1);

        assertEquals(new User(1, "vince"), foundUser);
    }

    @Test
    void findUserById_shouldReturnNullWhenIdDoesNotExist() {
        User foundUser = repository.findUserById(999);

        assertNull(foundUser);
    }

    private void clearUsersTable() {
        String sql = "DELETE FROM users";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to clear users table before test.", exception);
        }
    }
}