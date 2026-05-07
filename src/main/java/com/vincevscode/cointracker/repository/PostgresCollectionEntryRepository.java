// v0.4.0: PostgreSQL repository implementation for collection entry storage operations using JdbcTemplate.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.query.MissingCoinFilter;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinFilter;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PostgresCollectionEntryRepository implements CollectionEntryRepositoryInterface {
    private final JdbcTemplate jdbcTemplate;

    public PostgresCollectionEntryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CollectionEntry addCollectionEntry(int userId, int coinId, int quantity) {
        String sql = """
            INSERT INTO collection_entries (user_id, coin_id, quantity)
            VALUES (?, ?, ?)
            """;

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement statement = connection.prepareStatement(sql, new String[]{"id"});
            statement.setInt(1, userId);
            statement.setInt(2, coinId);
            statement.setInt(3, quantity);
            return statement;
        }, keyHolder);

        Number generatedId = keyHolder.getKey();

        if (generatedId == null) {
            throw new IllegalStateException("Failed to retrieve generated collection entry ID.");
        }

        return new CollectionEntry(generatedId.intValue(), userId, coinId, quantity);
    }

    @Override
    public List<CollectionEntry> getAllCollectionEntries() {
        String sql = """
                SELECT id, user_id, coin_id, quantity
                FROM collection_entries
                ORDER BY id
                """;

        return jdbcTemplate.query(sql, (resultSet, rowNumber) ->
                new CollectionEntry(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getInt("coin_id"),
                        resultSet.getInt("quantity")
                )
        );
    }

    @Override
    public CollectionEntry findCollectionEntryById(int id) {
        String sql = """
                SELECT id, user_id, coin_id, quantity
                FROM collection_entries
                WHERE id = ?
                """;

        List<CollectionEntry> results = jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new CollectionEntry(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getInt("coin_id"),
                        resultSet.getInt("quantity")
                ),
                id
        );

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public CollectionEntry findCollectionEntryByUserIdAndCoinId(int userId, int coinId) {
        String sql = """
                SELECT id, user_id, coin_id, quantity
                FROM collection_entries
                WHERE user_id = ? AND coin_id = ?
                """;

        List<CollectionEntry> results = jdbcTemplate.query(
                sql,
                (resultSet, rowNumber) -> new CollectionEntry(
                        resultSet.getInt("id"),
                        resultSet.getInt("user_id"),
                        resultSet.getInt("coin_id"),
                        resultSet.getInt("quantity")
                ),
                userId,
                coinId
        );

        return results.isEmpty() ? null : results.get(0);
    }

    @Override
    public boolean updateCollectionEntry(CollectionEntry updatedCollectionEntry) {
        String sql = """
                UPDATE collection_entries
                SET user_id = ?, coin_id = ?, quantity = ?
                WHERE id = ?
                """;

        int updatedRows = jdbcTemplate.update(
                sql,
                updatedCollectionEntry.getUserId(),
                updatedCollectionEntry.getCoinId(),
                updatedCollectionEntry.getQuantity(),
                updatedCollectionEntry.getId()
        );

        return updatedRows > 0;
    }

    @Override
    public List<OwnedCoinView> getOwnedCoinsForUser(int userId) {
        return getOwnedCoinsForUser(userId, (OwnedCoinQuery) null);
    }

    @Override
    public List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinFilter filter) {
        return getOwnedCoinsForUser(userId, new OwnedCoinQuery(filter, null, null, null));
    }

    @Override
    public List<OwnedCoinView> getOwnedCoinsForUser(int userId, OwnedCoinQuery query) {
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT c.id AS coin_id, c.country, c.denomination, c.year, ce.quantity
                FROM collection_entries ce
                JOIN coins c ON ce.coin_id = c.id
                WHERE ce.user_id = ? AND ce.quantity > 0
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(userId);

        OwnedCoinFilter filter = query != null ? query.getFilter() : null;
        if (filter != null) {
            if (filter.getCountry() != null && !filter.getCountry().isBlank()) {
                sqlBuilder.append(" AND c.country = ?");
                parameters.add(filter.getCountry());
            }

            if (filter.getDenomination() != null && !filter.getDenomination().isBlank()) {
                sqlBuilder.append(" AND c.denomination = ?");
                parameters.add(filter.getDenomination());
            }

            if (filter.getMinYear() != null) {
                sqlBuilder.append(" AND c.year >= ?");
                parameters.add(filter.getMinYear());
            }

            if (filter.getMaxYear() != null) {
                sqlBuilder.append(" AND c.year <= ?");
                parameters.add(filter.getMaxYear());
            }

            if (filter.getMinQuantity() != null) {
                sqlBuilder.append(" AND ce.quantity >= ?");
                parameters.add(filter.getMinQuantity());
            }
        }

        String sortField = "c.id";
        String sortDirection = "ASC";

        if (query != null && query.getSortField() != null) {
            sortField = query.getSortField().getSqlExpression();
        }

        if (query != null && query.getSortDirection() != null) {
            sortDirection = query.getSortDirection().name();
        }

        sqlBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection);

        if (query != null && query.getPageRequest() != null) {
            sqlBuilder.append(" LIMIT ? OFFSET ?");
            parameters.add(query.getPageRequest().getPageSize());
            parameters.add(query.getPageRequest().getOffset());
        }

        return jdbcTemplate.query(
                sqlBuilder.toString(),
                (resultSet, rowNumber) -> new OwnedCoinView(
                        resultSet.getInt("coin_id"),
                        resultSet.getString("country"),
                        resultSet.getString("denomination"),
                        resultSet.getInt("year"),
                        resultSet.getInt("quantity")
                ),
                parameters.toArray()
        );
    }

    @Override
    public List<MissingCoinView> getMissingCoinsForUser(int userId) {
        return getMissingCoinsForUser(userId, (MissingCoinQuery) null);
    }

    @Override
    public List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinFilter filter) {
        return getMissingCoinsForUser(userId, new MissingCoinQuery(filter, null, null, null));
    }

    @Override
    public List<MissingCoinView> getMissingCoinsForUser(int userId, MissingCoinQuery query) {
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT c.id AS coin_id, c.country, c.denomination, c.year
                FROM coins c
                LEFT JOIN collection_entries ce
                    ON c.id = ce.coin_id AND ce.user_id = ?
                WHERE ce.id IS NULL OR ce.quantity = 0
                """);

        List<Object> parameters = new ArrayList<>();
        parameters.add(userId);

        MissingCoinFilter filter = query != null ? query.getFilter() : null;
        if (filter != null) {
            if (filter.getCountry() != null && !filter.getCountry().isBlank()) {
                sqlBuilder.append(" AND c.country = ?");
                parameters.add(filter.getCountry());
            }

            if (filter.getDenomination() != null && !filter.getDenomination().isBlank()) {
                sqlBuilder.append(" AND c.denomination = ?");
                parameters.add(filter.getDenomination());
            }

            if (filter.getMinYear() != null) {
                sqlBuilder.append(" AND c.year >= ?");
                parameters.add(filter.getMinYear());
            }

            if (filter.getMaxYear() != null) {
                sqlBuilder.append(" AND c.year <= ?");
                parameters.add(filter.getMaxYear());
            }
        }

        String sortField = "c.id";
        String sortDirection = "ASC";

        if (query != null && query.getSortField() != null) {
            sortField = query.getSortField().getSqlExpression();
        }

        if (query != null && query.getSortDirection() != null) {
            sortDirection = query.getSortDirection().name();
        }

        sqlBuilder.append(" ORDER BY ").append(sortField).append(" ").append(sortDirection);

        if (query != null && query.getPageRequest() != null) {
            sqlBuilder.append(" LIMIT ? OFFSET ?");
            parameters.add(query.getPageRequest().getPageSize());
            parameters.add(query.getPageRequest().getOffset());
        }

        return jdbcTemplate.query(
                sqlBuilder.toString(),
                (resultSet, rowNumber) -> new MissingCoinView(
                        resultSet.getInt("coin_id"),
                        resultSet.getString("country"),
                        resultSet.getString("denomination"),
                        resultSet.getInt("year")
                ),
                parameters.toArray()
        );
    }
}