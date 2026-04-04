// v0.2.0: Basic PostgreSQL database connection helper using environment variables.
package com.vincevscode.cointracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = System.getenv("COIN_TRACKER_DB_URL");
    private static final String USERNAME = System.getenv("COIN_TRACKER_DB_USERNAME");
    private static final String PASSWORD = System.getenv("COIN_TRACKER_DB_PASSWORD");

    private DatabaseConnection() {
    }

    public static Connection getConnection() throws SQLException {
        validateConfiguration();

        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }

    private static void validateConfiguration() {
        if (URL == null || URL.isBlank()) {
            throw new IllegalStateException("Environment variable COIN_TRACKER_DB_URL is missing or blank.");
        }

        if (USERNAME == null || USERNAME.isBlank()) {
            throw new IllegalStateException("Environment variable COIN_TRACKER_DB_USERNAME is missing or blank.");
        }

        if (PASSWORD == null || PASSWORD.isBlank()) {
            throw new IllegalStateException("Environment variable COIN_TRACKER_DB_PASSWORD is missing or blank.");
        }
    }
}
