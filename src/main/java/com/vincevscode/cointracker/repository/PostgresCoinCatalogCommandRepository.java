// v0.9.0: PostgreSQL write implementation for the coin catalog.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.view.CoinCatalogView;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;

/**
 * JdbcTemplate implementation of the catalog writes. Inserts rely on the sequence default added
 * in {@code V5__coins_id_generated.sql} and read the id back via {@link GeneratedKeyHolder},
 * the same approach {@link PostgresAuthUserRepository#createAuthUser} uses for users.
 */
public class PostgresCoinCatalogCommandRepository implements CoinCatalogCommandRepositoryInterface {
    private static final RowMapper<CoinCatalogView> COIN_ROW_MAPPER = (resultSet, rowNumber) -> new CoinCatalogView(
            resultSet.getInt("id"),
            resultSet.getString("country"),
            resultSet.getString("denomination"),
            resultSet.getInt("year")
    );

    private final JdbcTemplate jdbcTemplate;

    public PostgresCoinCatalogCommandRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CoinCatalogView createCoin(String country, String denomination, int year) {
        String sql = "INSERT INTO coins (country, denomination, year) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setString(1, country);
            statement.setString(2, denomination);
            statement.setInt(3, year);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException("Failed to retrieve generated coin ID.");
        }

        return new CoinCatalogView(generatedId.intValue(), country, denomination, year);
    }

    @Override
    public CoinCatalogView updateCoin(int coinId, String country, String denomination, int year) {
        int updatedRows = jdbcTemplate.update(
                "UPDATE coins SET country = ?, denomination = ?, year = ? WHERE id = ?",
                country,
                denomination,
                year,
                coinId
        );

        if (updatedRows == 0) {
            return null;
        }

        return new CoinCatalogView(coinId, country, denomination, year);
    }

    @Override
    public boolean deleteCoin(int coinId) {
        return jdbcTemplate.update("DELETE FROM coins WHERE id = ?", coinId) > 0;
    }

    @Override
    public CoinCatalogView findCoinById(int coinId) {
        try {
            return jdbcTemplate.queryForObject(
                    "SELECT id, country, denomination, year FROM coins WHERE id = ?",
                    COIN_ROW_MAPPER,
                    coinId
            );
        } catch (EmptyResultDataAccessException exception) {
            return null;
        }
    }

    @Override
    public long countCollectionEntriesForCoin(int coinId) {
        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM collection_entries WHERE coin_id = ?",
                Long.class,
                coinId
        );

        return count == null ? 0 : count;
    }
}
