// v0.4.3: PostgreSQL repository implementation for auth-focused user loading operations using JdbcTemplate.
// v0.7.1: Adds admin management operations (list, update role/active, count admins).
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.util.List;

public class PostgresAuthUserRepository implements AuthUserRepositoryInterface {
    private static final RowMapper<AuthUser> AUTH_USER_ROW_MAPPER = (resultSet, rowNumber) -> new AuthUser(
            resultSet.getInt("id"),
            resultSet.getString("username"),
            resultSet.getString("password_hash"),
            UserRole.valueOf(resultSet.getString("role")),
            resultSet.getBoolean("is_active"),
            resultSet.getTimestamp("created_at").toLocalDateTime()
    );

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

        List<AuthUser> results = jdbcTemplate.query(sql, AUTH_USER_ROW_MAPPER, userId);

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public AuthUser findAuthUserByUsername(String username) {
        String sql = """
                SELECT id, username, password_hash, role, is_active, created_at
                FROM users
                WHERE username = ?
                """;

        List<AuthUser> results = jdbcTemplate.query(sql, AUTH_USER_ROW_MAPPER, username);

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public AuthUser createAuthUser(String username, String passwordHash, UserRole role, boolean active) {
        String sql = """
                INSERT INTO users (username, password_hash, role, is_active)
                VALUES (?, ?, ?, ?)
                """;

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            statement.setString(3, role.name());
            statement.setBoolean(4, active);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException("Failed to retrieve generated user ID.");
        }

        return findAuthUserById(generatedId.intValue());
    }

    @Override
    public List<AuthUser> getAllAuthUsers() {
        String sql = """
                SELECT id, username, password_hash, role, is_active, created_at
                FROM users
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, AUTH_USER_ROW_MAPPER);
    }

    @Override
    public AuthUser updateRole(int userId, UserRole role) {
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE users SET role = ? WHERE id = ?",
                role.name(),
                userId
        );

        return rowsUpdated == 0 ? null : findAuthUserById(userId);
    }

    @Override
    public AuthUser updateActive(int userId, boolean active) {
        int rowsUpdated = jdbcTemplate.update(
                "UPDATE users SET is_active = ? WHERE id = ?",
                active,
                userId
        );

        return rowsUpdated == 0 ? null : findAuthUserById(userId);
    }

    @Override
    public long countActiveAdmins() {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM users WHERE role = 'ADMIN' AND is_active = TRUE",
                Long.class
        );

        return count == null ? 0 : count;
    }
}
