// v0.3.0: PostgreSQL repository implementation for collection entry storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.db.DatabaseConnection;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresCollectionEntryRepository implements CollectionEntryRepositoryInterface {

    @Override
    public void addCollectionEntry(CollectionEntry collectionEntry) {
        String sql = """
                INSERT INTO collection_entries (id, user_id, coin_id, quantity)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, collectionEntry.getId());
            statement.setInt(2, collectionEntry.getUserId());
            statement.setInt(3, collectionEntry.getCoinId());
            statement.setInt(4, collectionEntry.getQuantity());

            statement.executeUpdate();

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to add collection entry to PostgreSQL.", exception);
        }
    }

    @Override
    public List<CollectionEntry> getAllCollectionEntries() {
        String sql = """
                SELECT id, user_id, coin_id, quantity
                FROM collection_entries
                ORDER BY id
                """;

        List<CollectionEntry> collectionEntries = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                int userId = resultSet.getInt("user_id");
                int coinId = resultSet.getInt("coin_id");
                int quantity = resultSet.getInt("quantity");

                collectionEntries.add(new CollectionEntry(id, userId, coinId, quantity));
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to retrieve collection entries from PostgreSQL.", exception);
        }

        return collectionEntries;
    }

    @Override
    public CollectionEntry findCollectionEntryById(int id) {
        String sql = """
                SELECT id, user_id, coin_id, quantity
                FROM collection_entries
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int entryId = resultSet.getInt("id");
                    int userId = resultSet.getInt("user_id");
                    int coinId = resultSet.getInt("coin_id");
                    int quantity = resultSet.getInt("quantity");

                    return new CollectionEntry(entryId, userId, coinId, quantity);
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find collection entry by ID in PostgreSQL.", exception);
        }

        return null;
    }

    @Override
    public CollectionEntry findCollectionEntryByUserIdAndCoinId(int userId, int coinId) {
        String sql = """
                SELECT id, user_id, coin_id, quantity
                FROM collection_entries
                WHERE user_id = ? AND coin_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);
            statement.setInt(2, coinId);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int entryId = resultSet.getInt("id");
                    int foundUserId = resultSet.getInt("user_id");
                    int foundCoinId = resultSet.getInt("coin_id");
                    int quantity = resultSet.getInt("quantity");

                    return new CollectionEntry(entryId, foundUserId, foundCoinId, quantity);
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find collection entry by user ID and coin ID in PostgreSQL.", exception);
        }

        return null;
    }

    @Override
    public boolean updateCollectionEntry(CollectionEntry updatedCollectionEntry) {
        String sql = """
                UPDATE collection_entries
                SET user_id = ?, coin_id = ?, quantity = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, updatedCollectionEntry.getUserId());
            statement.setInt(2, updatedCollectionEntry.getCoinId());
            statement.setInt(3, updatedCollectionEntry.getQuantity());
            statement.setInt(4, updatedCollectionEntry.getId());

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update collection entry in PostgreSQL.", exception);
        }
    }

    @Override
    public List<OwnedCoinView> getOwnedCoinsForUser(int userId) {
        String sql = """
                SELECT c.id AS coin_id, c.country, c.denomination, c.year, ce.quantity
                FROM collection_entries ce
                JOIN coins c ON ce.coin_id = c.id
                WHERE ce.user_id = ? AND ce.quantity > 0
                ORDER BY c.id
                """;

        List<OwnedCoinView> ownedCoins = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int coinId = resultSet.getInt("coin_id");
                    String country = resultSet.getString("country");
                    String denomination = resultSet.getString("denomination");
                    int year = resultSet.getInt("year");
                    int quantity = resultSet.getInt("quantity");

                    ownedCoins.add(new OwnedCoinView(
                            coinId,
                            country,
                            denomination,
                            year,
                            quantity
                    ));
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to retrieve owned coins for user from PostgreSQL.", exception);
        }

        return ownedCoins;
    }

    @Override
    public List<MissingCoinView> getMissingCoinsForUser(int userId) {
        String sql = """
                SELECT c.id AS coin_id, c.country, c.denomination, c.year
                FROM coins c
                LEFT JOIN collection_entries ce
                    ON c.id = ce.coin_id AND ce.user_id = ?
                WHERE ce.id IS NULL OR ce.quantity = 0
                ORDER BY c.id
                """;

        List<MissingCoinView> missingCoins = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    int coinId = resultSet.getInt("coin_id");
                    String country = resultSet.getString("country");
                    String denomination = resultSet.getString("denomination");
                    int year = resultSet.getInt("year");

                    missingCoins.add(new MissingCoinView(
                            coinId,
                            country,
                            denomination,
                            year
                    ));
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to retrieve missing coins for user from PostgreSQL.", exception);
        }

        return missingCoins;
    }
}