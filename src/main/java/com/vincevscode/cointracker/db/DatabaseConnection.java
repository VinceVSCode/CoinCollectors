package com.vincevscode.cointracker.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    // later to be made environment variables or config file later, PostgresSQL not yet configured.
    private static final String URL = "jdbc:postgresql://localhost:5432/coin_tracker";
    private static final String USERNAME = "postgres";
    private static final String PASSWORD = "postgres";

    private DatabaseConnection() {
        // This is the DatabaseConnection section
    }
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USERNAME, PASSWORD);
    }
}
