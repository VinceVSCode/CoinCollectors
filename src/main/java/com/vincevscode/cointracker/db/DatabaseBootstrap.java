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

/**
 * Runs once, synchronously, before the Spring context starts (see {@link com.vincevscode.cointracker.App#main}),
 * so tables exist by the time any repository bean tries to use them. Three independent steps,
 * each opt-in via env var except migrations which always run: schema migration (Flyway,
 * always), an optional full data wipe, and an optional seed load — see docker-compose.yml for
 * how COIN_TRACKER_DB_RESET_ON_START / _SEED_ON_START get set for local dev.
 * KNOWN GOTCHA: seed_data.sql is not idempotent (plain INSERTs, no ON CONFLICT), so re-running
 * `docker compose up` against a persisted volume without `down -v` first will crash on a
 * duplicate-key violation. Always `docker compose down -v` before `up` until that's fixed.
 */
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

        // baselineOnMigrate: lets Flyway adopt a database that already has tables but no
        // Flyway history (e.g. one created by the legacy db/00x_*.sql scripts) instead of
        // refusing to run — see src/main/resources/db/README.md for that migration story.
        Flyway flyway = Flyway.configure()
                .dataSource(config.getUrl(), config.getUsername(), config.getPassword())
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    private static void runSqlScript(String resourcePath) {
        // resourcePath is always one of the two hard-coded literals above, never user input,
        // so executing the whole file as one statement is safe here despite not being
        // parameterized — this is trusted bundled SQL, not a query built from external data.
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