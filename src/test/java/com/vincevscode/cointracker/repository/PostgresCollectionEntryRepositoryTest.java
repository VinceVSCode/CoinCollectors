// v0.4.0: Integration tests for PostgreSQL-backed collection entry repository behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.config.DatabaseConfig;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PostgresCollectionEntryRepositoryTest {
    private PostgresCollectionEntryRepository repository;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        System.out.println("DB URL: " + DatabaseConfig.fromEnvironment().getUrl());
        jdbcTemplate = createJdbcTemplate();
        runMigrations();
        repository = new PostgresCollectionEntryRepository(jdbcTemplate);

        clearCollectionEntriesTable();
        resetCollectionEntrySequence();
        clearUsersTable();
        clearCoinsTable();
        seedRequiredData();
    }

    @Test
    void addCollectionEntry_shouldStoreEntryInDatabase() {
        CollectionEntry createdEntry = repository.addCollectionEntry(1, 1, 2);

        CollectionEntry foundEntry = repository.findCollectionEntryById(createdEntry.getId());

        assertTrue(createdEntry.getId() > 0);
        assertEquals(createdEntry, foundEntry);
    }

    @Test
    void getAllCollectionEntries_shouldReturnAllStoredEntries() {
        CollectionEntry firstEntry = repository.addCollectionEntry(1, 1, 2);
        CollectionEntry secondEntry = repository.addCollectionEntry(1, 2, 0);

        List<CollectionEntry> collectionEntries = repository.getAllCollectionEntries();

        assertEquals(2, collectionEntries.size());
        assertEquals(firstEntry, collectionEntries.get(0));
        assertEquals(secondEntry, collectionEntries.get(1));
    }

    @Test
    void findCollectionEntryById_shouldReturnEntryWhenIdExists() {
        CollectionEntry createdEntry = repository.addCollectionEntry(1, 1, 2);

        CollectionEntry foundEntry = repository.findCollectionEntryById(createdEntry.getId());

        assertEquals(createdEntry, foundEntry);
    }

    @Test
    void findCollectionEntryById_shouldReturnNullWhenIdDoesNotExist() {
        CollectionEntry foundEntry = repository.findCollectionEntryById(999);

        assertNull(foundEntry);
    }

    @Test
    void findCollectionEntryByUserIdAndCoinId_shouldReturnEntryWhenPairExists() {
        CollectionEntry createdEntry = repository.addCollectionEntry(1, 2, 3);

        CollectionEntry foundEntry = repository.findCollectionEntryByUserIdAndCoinId(1, 2);

        assertEquals(createdEntry, foundEntry);
    }

    @Test
    void findCollectionEntryByUserIdAndCoinId_shouldReturnNullWhenPairDoesNotExist() {
        CollectionEntry foundEntry = repository.findCollectionEntryByUserIdAndCoinId(99, 99);

        assertNull(foundEntry);
    }

    @Test
    void updateCollectionEntry_shouldReturnTrueWhenEntryExists() {
        CollectionEntry createdEntry = repository.addCollectionEntry(1, 1, 2);

        boolean updated = repository.updateCollectionEntry(
                new CollectionEntry(createdEntry.getId(), 1, 1, 5)
        );

        assertTrue(updated);
        assertEquals(
                new CollectionEntry(createdEntry.getId(), 1, 1, 5),
                repository.findCollectionEntryById(createdEntry.getId())
        );
    }

    @Test
    void getOwnedCoinsForUser_shouldReturnOnlyCoinsWithPositiveQuantity() {
        repository.addCollectionEntry(1, 1, 2);
        repository.addCollectionEntry(1, 2, 0);

        List<OwnedCoinView> ownedCoins = repository.getOwnedCoinsForUser(1);

        assertEquals(1, ownedCoins.size());
        assertEquals(
                new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2),
                ownedCoins.get(0)
        );
    }

    @Test
    void getMissingCoinsForUser_shouldReturnCoinsWithoutEntry() {
        repository.addCollectionEntry(1, 1, 2);

        List<MissingCoinView> missingCoins = repository.getMissingCoinsForUser(1);

        assertEquals(1, missingCoins.size());
        assertEquals(
                new MissingCoinView(2, "Germany", "1 Euro", 2010),
                missingCoins.get(0)
        );
    }

    @Test
    void getMissingCoinsForUser_shouldReturnCoinsWithZeroQuantity() {
        repository.addCollectionEntry(1, 1, 2);
        repository.addCollectionEntry(1, 2, 0);

        List<MissingCoinView> missingCoins = repository.getMissingCoinsForUser(1);

        assertEquals(1, missingCoins.size());
        assertEquals(
                new MissingCoinView(2, "Germany", "1 Euro", 2010),
                missingCoins.get(0)
        );
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

    private void seedRequiredData() {
        jdbcTemplate.update(
                """
                INSERT INTO users (id, username)
                VALUES (1, 'vince')
                """
        );

        jdbcTemplate.update(
                """
                INSERT INTO coins (id, country, denomination, year)
                VALUES
                    (1, 'Bulgaria', '1 Lev', 2002),
                    (2, 'Germany', '1 Euro', 2010)
                """
        );
    }

    private void clearCollectionEntriesTable() {
        jdbcTemplate.update("DELETE FROM collection_entries");
    }

    private void resetCollectionEntrySequence() {
        jdbcTemplate.execute("ALTER SEQUENCE collection_entries_id_seq RESTART WITH 1");
    }

    private void clearUsersTable() {
        jdbcTemplate.update("DELETE FROM users");
    }

    private void clearCoinsTable() {
        jdbcTemplate.update("DELETE FROM coins");
    }
}