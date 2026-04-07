// v0.2.6: Basic PostgreSQL database connection helper using DatabaseConfig.
package com.vincevscode.cointracker.db;

import com.vincevscode.cointracker.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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