// v0.4.4: Integration tests for PostgreSQL-backed auth user repository behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.config.DatabaseConfig;
import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Integration test: needs a real, reachable Postgres (COIN_TRACKER_DB_* env vars) with the
// schema migrated — see env_native_postgres_port_conflict in the project notes for why running
// this via plain `mvn test` on a host that also has a native Postgres on :5432 can silently hit
// the wrong database; running inside the docker-compose network is the reliable path.
class PostgresAuthUserRepositoryTest {
    private PostgresAuthUserRepository repository;
    private JdbcTemplate jdbcTemplate;

    // Each test starts from the same known two-row state (vince=ADMIN id 1, alex=USER id 2)
    // rather than an empty table, since most of these tests need at least one existing user
    // to look up or mutate.
    @BeforeEach
    void setUp() {
        jdbcTemplate = createJdbcTemplate();
        runMigrations();
        repository = new PostgresAuthUserRepository(jdbcTemplate);

        clearUsersTable();
        resetUsersSequence();
        seedUsers();
    }

    @Test
    void createAuthUser_shouldInsertAndReturnNewUserWithGeneratedId() {
        AuthUser createdUser = repository.createAuthUser("newuser", "hashed-password", UserRole.USER, true);

        assertTrue(createdUser.getId() > 0);
        assertEquals("newuser", createdUser.getUsername());
        assertEquals("hashed-password", createdUser.getPasswordHash());
        assertEquals(UserRole.USER, createdUser.getRole());
        assertTrue(createdUser.isActive());

        AuthUser foundUser = repository.findAuthUserById(createdUser.getId());
        assertEquals(createdUser, foundUser);
    }

    @Test
    void findAuthUserById_shouldReturnUserWhenIdExists() {
        AuthUser foundUser = repository.findAuthUserById(1);

        assertEquals(1, foundUser.getId());
        assertEquals("vince", foundUser.getUsername());
        assertEquals("hashed-password", foundUser.getPasswordHash());
        assertEquals(UserRole.ADMIN, foundUser.getRole());
        assertTrue(foundUser.isActive());
    }

    @Test
    void findAuthUserById_shouldReturnNullWhenIdDoesNotExist() {
        AuthUser foundUser = repository.findAuthUserById(999);

        assertNull(foundUser);
    }

    @Test
    void findAuthUserByUsername_shouldReturnUserWhenUsernameExists() {
        AuthUser foundUser = repository.findAuthUserByUsername("alex");

        assertEquals(2, foundUser.getId());
        assertEquals("alex", foundUser.getUsername());
        assertEquals(UserRole.USER, foundUser.getRole());
    }

    @Test
    void findAuthUserByUsername_shouldReturnNullWhenUsernameDoesNotExist() {
        AuthUser foundUser = repository.findAuthUserByUsername("nobody");

        assertNull(foundUser);
    }

    @Test
    void getAllAuthUsers_shouldReturnAllUsersOrderedById() {
        List<AuthUser> users = repository.getAllAuthUsers();

        assertEquals(2, users.size());
        assertEquals("vince", users.get(0).getUsername());
        assertEquals("alex", users.get(1).getUsername());
    }

    @Test
    void updateRole_shouldChangeRoleAndReturnUpdatedUser() {
        AuthUser updated = repository.updateRole(2, UserRole.ADMIN);

        assertEquals(UserRole.ADMIN, updated.getRole());
        assertEquals(UserRole.ADMIN, repository.findAuthUserById(2).getRole());
    }

    @Test
    void updateRole_shouldReturnNullWhenUserDoesNotExist() {
        assertNull(repository.updateRole(999, UserRole.ADMIN));
    }

    @Test
    void updateActive_shouldChangeActiveFlagAndReturnUpdatedUser() {
        AuthUser updated = repository.updateActive(2, false);

        assertFalse(updated.isActive());
        assertFalse(repository.findAuthUserById(2).isActive());
    }

    @Test
    void countActiveAdmins_shouldCountOnlyActiveAdmins() {
        assertEquals(1, repository.countActiveAdmins());

        repository.updateRole(2, UserRole.ADMIN);
        assertEquals(2, repository.countActiveAdmins());

        repository.updateActive(1, false);
        assertEquals(1, repository.countActiveAdmins());
    }

    private JdbcTemplate createJdbcTemplate() {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();

        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(databaseConfig.getUrl());
        dataSource.setUsername(databaseConfig.getUsername());
        dataSource.setPassword(databaseConfig.getPassword());

        return new JdbcTemplate(dataSource);
    }

    private void runMigrations() {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();

        Flyway flyway = Flyway.configure()
                .dataSource(
                        databaseConfig.getUrl(),
                        databaseConfig.getUsername(),
                        databaseConfig.getPassword()
                )
                .locations("classpath:db/migration")
                .baselineOnMigrate(true)
                .load();

        flyway.migrate();
    }

    private void clearUsersTable() {
        jdbcTemplate.update("DELETE FROM users");
    }

    private void resetUsersSequence() {
        jdbcTemplate.execute("ALTER SEQUENCE users_id_seq RESTART WITH 1");
    }

    private void seedUsers() {
        jdbcTemplate.update(
                """
                INSERT INTO users (username, password_hash, role, is_active)
                VALUES
                    ('vince', 'hashed-password', 'ADMIN', TRUE),
                    ('alex', 'hashed-password', 'USER', TRUE)
                """
        );
    }
}
