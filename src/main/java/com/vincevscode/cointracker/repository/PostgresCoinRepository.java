// v0.2.0: PostgreSQL repository implementation for coin storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.db.DatabaseConnection;
import com.vincevscode.cointracker.model.Coin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresCoinRepository implements CoinRepositoryInterface {

    @Override
    public void addCoin(Coin coin) {
        String sql = """
                INSERT INTO coins (id, country, denomination, year)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, coin.getId());
            statement.setString(2, coin.getCountry());
            statement.setString(3, coin.getDenomination());
            statement.setInt(4, coin.getYear());

            statement.executeUpdate();


        } catch (SQLException exception) {
            throw new RuntimeException("Failed to add coin to PostgreSQL", exception);
        }
    }

    @Override
    public List<Coin> getAllCoins() {
        String sql = """
                SELECT id, country, denomination, year 
                FROM coins
                ORDER BY id
                """;

        List<Coin> coins = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()){

            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String country = resultSet.getString("country");
                String denomination = resultSet.getString("denomination");
                int year = resultSet.getInt("year");

                Coin coin = new Coin(id, country, denomination, year);
                coins.add(coin);
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to retrieve coins from PostgreSQL", exception);
        }
        return coins;
    }

    @Override
    public Coin findCoinById(int id) {
        String sql = """
                SELECT id, country, denomination, year
                FROM coins
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    int coinId = resultSet.getInt("id");
                    String country = resultSet.getString("country");
                    String denomination = resultSet.getString("denomination");
                    int year = resultSet.getInt("year");

                    return new Coin(coinId, country, denomination, year);
                }
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to find coin by ID " + id +" in PostgreSQL.", exception);
        }

        return null;
    }

    @Override
    public boolean updateCoin(Coin updatedCoin) {
        String sql = """
                UPDATE coins
                SET country = ?, denomination = ?, year = ?
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, updatedCoin.getCountry());
            statement.setString(2, updatedCoin.getDenomination());
            statement.setInt(3, updatedCoin.getYear());
            statement.setInt(4, updatedCoin.getId());

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to update coin in PostgreSQL.", exception);
        }
    }

    @Override
    public boolean removeCoinById(int id) {
        String sql = """
                DELETE FROM coins
                WHERE id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, id);

            int affectedRows = statement.executeUpdate();

            return affectedRows > 0;

        } catch (SQLException exception) {
            throw new RuntimeException("Failed to remove with ID "+ id +" coin from PostgreSQL.", exception);
        }
    }
}