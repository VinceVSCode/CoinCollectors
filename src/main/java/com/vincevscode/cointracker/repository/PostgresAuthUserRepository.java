// v0.4.3: PostgreSQL repository implementation for auth-focused user loading operations using JdbcTemplate.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class PostgresAuthUserRepository implements AuthUserRepositoryInterface {
    private final JdbcTemplate jdbcTemplate;

    public PostgresAuthUserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public AuthUser findAuthUserById(int userId) {
        String sql = """
                SELECT id, username, password_hash, role, is_active, created_at
                FROM users
                WHERE id = ?
                """;

        List<AuthUser> results = jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new AuthUser(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        UserRole.valueOf(resultSet.getString("role")),
                        resultSet.getBoolean("is_active"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                ),
                userId
        );

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public AuthUser findAuthUserByUsername(String username) {
        String sql = """
                SELECT id, username, password_hash, role, is_active, created_at
                FROM users
                WHERE username = ?
                """;

        List<AuthUser> results = jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new AuthUser(
                        resultSet.getInt("id"),
                        resultSet.getString("username"),
                        resultSet.getString("password_hash"),
                        UserRole.valueOf(resultSet.getString("role")),
                        resultSet.getBoolean("is_active"),
                        resultSet.getTimestamp("created_at").toLocalDateTime()
                ),
                username
        );

        return results.isEmpty() ? null : results.get(0);
    }
}