// v0.3.0: Integration tests for PostgreSQL-backed collection entry repository behavior.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.db.DatabaseConnection;
import com.vincevscode.cointracker.model.CollectionEntry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PostgresCollectionEntryRepositoryTest {
    private PostgresCollectionEntryRepository repository;

    @BeforeEach
    void setUp() {
        repository = new PostgresCollectionEntryRepository();
        clearCollectionEntriesTable();
        clearUsersTable();
        clearCoinsTable();
        seedRequiredData();
    }

    @Test
    void addCollectionEntry_shouldStoreEntryInDatabase() {
        CollectionEntry collectionEntry = new CollectionEntry(1, 1, 1, 2);

        repository.addCollectionEntry(collectionEntry);

        CollectionEntry foundEntry = repository.findCollectionEntryById(1);

        assertEquals(collectionEntry, foundEntry);
    }

    @Test
    void getAllCollectionEntries_shouldReturnAllStoredEntries() {
        repository.addCollectionEntry(new CollectionEntry(1, 1, 1, 2));
        repository.addCollectionEntry(new CollectionEntry(2, 1, 2, 0));

        List<CollectionEntry> collectionEntries = repository.getAllCollectionEntries();

        assertEquals(2, collectionEntries.size());
        assertEquals(new CollectionEntry(1, 1, 1, 2), collectionEntries.get(0));
        assertEquals(new CollectionEntry(2, 1, 2, 0), collectionEntries.get(1));
    }

    @Test
    void findCollectionEntryById_shouldReturnEntryWhenIdExists() {
        repository.addCollectionEntry(new CollectionEntry(1, 1, 1, 2));

        CollectionEntry foundEntry = repository.findCollectionEntryById(1);

        assertEquals(new CollectionEntry(1, 1, 1, 2), foundEntry);
    }

    @Test
    void findCollectionEntryById_shouldReturnNullWhenIdDoesNotExist() {
        CollectionEntry foundEntry = repository.findCollectionEntryById(999);

        assertNull(foundEntry);
    }

    @Test
    void findCollectionEntryByUserIdAndCoinId_shouldReturnEntryWhenPairExists() {
        repository.addCollectionEntry(new CollectionEntry(1, 1, 2, 3));

        CollectionEntry foundEntry = repository.findCollectionEntryByUserIdAndCoinId(1, 2);

        assertEquals(new CollectionEntry(1, 1, 2, 3), foundEntry);
    }

    @Test
    void findCollectionEntryByUserIdAndCoinId_shouldReturnNullWhenPairDoesNotExist() {
        CollectionEntry foundEntry = repository.findCollectionEntryByUserIdAndCoinId(99, 99);

        assertNull(foundEntry);
    }

    private void seedRequiredData() {
        String insertUserSql = """
                INSERT INTO users (id, username)
                VALUES (1, 'vince')
                """;

        String insertCoinsSql = """
                INSERT INTO coins (id, country, denomination, year)
                VALUES
                    (1, 'Bulgaria', '1 Lev', 2002),
                    (2, 'Germany', '1 Euro', 2010)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement userStatement = connection.prepareStatement(insertUserSql);
             PreparedStatement coinsStatement = connection.prepareStatement(insertCoinsSql)) {

            userStatement.executeUpdate();
            coinsStatement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to seed required test data.", exception);
        }
    }

    private void clearCollectionEntriesTable() {
        String sql = "DELETE FROM collection_entries";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to clear collection_entries table before test.", exception);
        }
    }

    private void clearUsersTable() {
        String sql = "DELETE FROM users";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to clear users table before test.", exception);
        }
    }

    private void clearCoinsTable() {
        String sql = "DELETE FROM coins";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to clear coins table before test.", exception);
        }
    }
}