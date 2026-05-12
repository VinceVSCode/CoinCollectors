// v0.4.2: PostgreSQL repository implementation for catalog coin screen queries using JdbcTemplate.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.query.CoinCatalogFilter;
import com.vincevscode.cointracker.query.CoinCatalogQuery;
import com.vincevscode.cointracker.view.CoinCatalogView;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;

public class PostgresCoinCatalogQueryRepository implements CoinCatalogQueryRepositoryInterface {
    private final JdbcTemplate jdbcTemplate;

    public PostgresCoinCatalogQueryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<CoinCatalogView> getCoins(CoinCatalogQuery query) {
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT c.id AS coin_id, c.country, c.denomination, c.year
                FROM coins c
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();

        CoinCatalogFilter filter = query != null ? query.getFilter() : null;
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
                (resultSet, rowNumber) -> new CoinCatalogView(
                        resultSet.getInt("coin_id"),
                        resultSet.getString("country"),
                        resultSet.getString("denomination"),
                        resultSet.getInt("year")
                ),
                parameters.toArray()
        );
    }

    @Override
    public long countCoins(CoinCatalogFilter filter) {
        StringBuilder sqlBuilder = new StringBuilder("""
                SELECT COUNT(*)
                FROM coins c
                WHERE 1 = 1
                """);

        List<Object> parameters = new ArrayList<>();

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

        Long count = jdbcTemplate.queryForObject(
                sqlBuilder.toString(),
                Long.class,
                parameters.toArray()
        );

        return count == null ? 0 : count;
    }
}