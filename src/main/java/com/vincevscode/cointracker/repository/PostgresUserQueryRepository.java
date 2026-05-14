// v0.4.3: PostgreSQL repository implementation for user query operations using JdbcTemplate.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.view.UserView;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

public class PostgresUserQueryRepository implements UserQueryRepositoryInterface {
    private final JdbcTemplate jdbcTemplate;

    public PostgresUserQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<UserView> getUsers() {
        String sql = """
                SELECT id, username
                FROM users
                ORDER BY id
                """;

        return jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new UserView(
                        resultSet.getInt("id"),
                        resultSet.getString("username")
                )
        );
    }

    @Override
    public UserView getUserById(int userId) {
        String sql = """
                SELECT id, username
                FROM users
                WHERE id = ?
                """;

        List<UserView> results = jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new UserView(
                        resultSet.getInt("id"),
                        resultSet.getString("username")
                ),
                userId
        );

        return results.isEmpty() ? null : results.get(0);
    }
}