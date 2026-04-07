// v0.2.6: Database configuration loaded from environment variables.
package com.vincevscode.cointracker.config;

public class DatabaseConfig {
    private final String url;
    private final String username;
    private final String password;

    public DatabaseConfig(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static DatabaseConfig fromEnvironment() {
        String url = System.getenv("COIN_TRACKER_DB_URL");
        String username = System.getenv("COIN_TRACKER_DB_USERNAME");
        String password = System.getenv("COIN_TRACKER_DB_PASSWORD");

        validate(url, username, password);

        return new DatabaseConfig(url, username, password);
    }

    private static void validate(String url, String username, String password) {
        if (url == null || url.isBlank()) {
            throw new IllegalStateException("Environment variable COIN_TRACKER_DB_URL is missing or blank.");
        }

        if (username == null || username.isBlank()) {
            throw new IllegalStateException("Environment variable COIN_TRACKER_DB_USERNAME is missing or blank.");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalStateException("Environment variable COIN_TRACKER_DB_PASSWORD is missing or blank.");
        }
    }

    public String getUrl() {
        return url;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }
}