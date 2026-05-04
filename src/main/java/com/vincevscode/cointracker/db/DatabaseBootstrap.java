// v0.3.4: Database bootstrap utility for migration, reset, and seed operations.
package com.vincevscode.cointracker.db;

import com.vincevscode.cointracker.config.DatabaseConfig;
import org.flywaydb.core.Flyway;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.stream.Collectors;

public class DatabaseBootstrap {

    private DatabaseBootstrap() {
    }

    public static void initialize() {
        runMigrations();

        if (isEnabled("COIN_TRACKER_DB_RESET_ON_START")) {
            runSqlScript("db/reset/reset_data.sql");
        }

        if (isEnabled("COIN_TRACKER_DB_SEED_ON_START")) {
            runSqlScript("db/seed/seed_data.sql");
        }
    }

    private static void runMigrations() {
        DatabaseConfig config = DatabaseConfig.fromEnvironment();

        Flyway flyway = Flyway.configure()
                .dataSource(config.getUrl(), config.getUsername(), config.getPassword())
                .locations("src/main/resources/db/migration/V1__initial_schema.sql")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    private static void runSqlScript(String resourcePath) {
        String sql = loadResourceFile(resourcePath);

        try (Connection connection = DatabaseConnection.getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(sql);

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to execute SQL script: " + resourcePath, exception);
        }
    }

    private static String loadResourceFile(String resourcePath) {
        InputStream inputStream = DatabaseBootstrap.class
                .getClassLoader()
                .getResourceAsStream(resourcePath);

        if (inputStream == null) {
            throw new IllegalStateException("Resource not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            return reader.lines().collect(Collectors.joining("\n"));
        } catch (IOException exception) {
            throw new RuntimeException("Failed to read resource file: " + resourcePath, exception);
        }
    }

    private static boolean isEnabled(String environmentVariableName) {
        String value = System.getenv(environmentVariableName);
        return value != null && value.equalsIgnoreCase("true");
    }
}