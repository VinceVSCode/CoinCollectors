// v0.3.0: Integration tests for CollectionTrackingService behavior with PostgreSQL.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.db.DatabaseConnection;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.repository.PostgresCollectionEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CollectionTrackingServiceTest {
    private CollectionTrackingService service;

    @BeforeEach
    void setUp() {
        service = new CollectionTrackingService(new PostgresCollectionEntryRepository());
        clearCollectionEntriesTable();
        clearUsersTable();
        clearCoinsTable();
        seedRequiredData();
    }

    @Test
    void setCoinQuantity_shouldCreateNewEntryWhenEntryDoesNotExist() {
        service.setCoinQuantity(1, 1, 1, 2);

        CollectionEntry foundEntry = service.findCollectionEntryByUserIdAndCoinId(1, 1);

        assertEquals(new CollectionEntry(1, 1, 1, 2), foundEntry);
    }

    @Test
    void setCoinQuantity_shouldUpdateExistingEntryWhenEntryAlreadyExists() {
        service.setCoinQuantity(1, 1, 1, 2);

        service.setCoinQuantity(99, 1, 1, 5);

        CollectionEntry foundEntry = service.findCollectionEntryByUserIdAndCoinId(1, 1);

        assertEquals(new CollectionEntry(1, 1, 1, 5), foundEntry);
    }

    @Test
    void setCoinQuantity_shouldThrowExceptionWhenQuantityIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.setCoinQuantity(1, 1, 1, -1)
        );

        assertEquals("Quantity cannot be negative.", exception.getMessage());
    }

    private void seedRequiredData() {
        String insertUserSql = """
                INSERT INTO users (id, username)
                VALUES (1, 'vince')
                """;

        String insertCoinSql = """
                INSERT INTO coins (id, country, denomination, year)
                VALUES (1, 'Bulgaria', '1 Lev', 2002)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement userStatement = connection.prepareStatement(insertUserSql);
             PreparedStatement coinStatement = connection.prepareStatement(insertCoinSql)) {

            userStatement.executeUpdate();
            coinStatement.executeUpdate();

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