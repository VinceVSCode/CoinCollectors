// v0.2.6: Basic PostgreSQL database connection helper using DatabaseConfig.
package com.vincevscode.cointracker.db;

import com.vincevscode.cointracker.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Opens a brand-new, unpooled JDBC connection per call via {@link DriverManager} — used by
 * the legacy raw-JDBC repositories ({@link com.vincevscode.cointracker.repository.PostgresCoinRepository},
 * {@link com.vincevscode.cointracker.repository.PostgresUserRepository}) and
 * {@link com.vincevscode.cointracker.db.DatabaseBootstrap}. The newer JdbcTemplate-based
 * repositories don't use this — they share the pooled HikariCP DataSource bean from
 * {@link com.vincevscode.cointracker.config.ApplicationConfiguration} instead, which is why
 * this class still exists alongside a connection pool rather than being replaced by it.
 */
public class DatabaseConnection {

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();

        return DriverManager.getConnection(
                databaseConfig.getUrl(),
                databaseConfig.getUsername(),
                databaseConfig.getPassword()
        );
    }
}