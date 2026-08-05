// v0.10.0: PostgreSQL implementation of the admin audit trail.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.AdminActionType;
import com.vincevscode.cointracker.model.AdminActor;
import com.vincevscode.cointracker.view.AdminActionView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.Timestamp;
import java.util.List;

/**
 * JdbcTemplate implementation over {@code admin_actions} (see {@code V6__admin_actions.sql}).
 * {@code created_at} is left to the column's {@code DEFAULT NOW()} so the timestamp comes from
 * the database clock — with the app clock, a container with skewed time could write records that
 * appear out of order relative to everything else in the table.
 */
public class PostgresAdminAuditRepository implements AdminAuditRepositoryInterface {
    private static final RowMapper<AdminActionView> ACTION_ROW_MAPPER = (resultSet, rowNumber) -> {
        int targetId = resultSet.getInt("target_id");
        Timestamp createdAt = resultSet.getTimestamp("created_at");

        return new AdminActionView(
                resultSet.getLong("id"),
                resultSet.getInt("actor_user_id"),
                resultSet.getString("actor_username"),
                // Read back as the raw string, not AdminActionType.valueOf: a record written by a
                // build that knew an action this one doesn't must still be listable.
                resultSet.getString("action"),
                resultSet.getString("target_type"),
                resultSet.wasNull() ? null : targetId,
                resultSet.getString("details"),
                createdAt == null ? null : createdAt.toLocalDateTime()
        );
    };

    private final JdbcTemplate jdbcTemplate;

    public PostgresAdminAuditRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void recordAction(
            AdminActor actor,
            AdminActionType action,
            String targetType,
            Integer targetId,
            String details
    ) {
        jdbcTemplate.update(
                """
                INSERT INTO admin_actions (actor_user_id, actor_username, action, target_type, target_id, details)
                VALUES (?, ?, ?, ?, ?, ?)
                """,
                actor.getUserId(),
                actor.getUsername(),
                action.name(),
                targetType,
                targetId,
                details
        );
    }

    @Override
    public List<AdminActionView> getRecentActions(int limit) {
        return jdbcTemplate.query(
                """
                SELECT id, actor_user_id, actor_username, action, target_type, target_id, details, created_at
                FROM admin_actions
                ORDER BY created_at DESC, id DESC
                LIMIT ?
                """,
                ACTION_ROW_MAPPER,
                limit
        );
    }
}
