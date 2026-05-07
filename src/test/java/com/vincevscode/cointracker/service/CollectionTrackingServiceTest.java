// v0.4.0: Integration tests for CollectionTrackingService behavior with PostgreSQL.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.config.DatabaseConfig;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.query.MissingCoinFilter;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.MissingCoinSortField;
import com.vincevscode.cointracker.query.OwnedCoinFilter;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinSortField;
import com.vincevscode.cointracker.query.PageRequest;
import com.vincevscode.cointracker.query.SortDirection;
import com.vincevscode.cointracker.repository.PostgresCollectionEntryRepository;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CollectionTrackingServiceTest {
    private CollectionTrackingService service;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        System.out.println("DB URL: " + DatabaseConfig.fromEnvironment().getUrl());
        jdbcTemplate = createJdbcTemplate();
        runMigrations();
        service = new CollectionTrackingService(new PostgresCollectionEntryRepository(jdbcTemplate));

        clearCollectionEntriesTable();
        resetCollectionEntrySequence();
        clearUsersTable();
        clearCoinsTable();
        seedRequiredData();
    }

    @Test
    void setCoinQuantity_shouldCreateNewEntryWhenEntryDoesNotExist() {
        CollectionEntry createdEntry = service.setCoinQuantity(1, 1, 2);

        CollectionEntry foundEntry = service.findCollectionEntryByUserIdAndCoinId(1, 1);

        assertTrue(createdEntry.getId() > 0);
        assertEquals(createdEntry, foundEntry);
        assertEquals(1, foundEntry.getUserId());
        assertEquals(1, foundEntry.getCoinId());
        assertEquals(2, foundEntry.getQuantity());
    }

    @Test
    void setCoinQuantity_shouldUpdateExistingEntryWhenEntryAlreadyExists() {
        CollectionEntry firstEntry = service.setCoinQuantity(1, 1, 2);
        CollectionEntry updatedEntry = service.setCoinQuantity(1, 1, 5);

        CollectionEntry foundEntry = service.findCollectionEntryByUserIdAndCoinId(1, 1);

        assertEquals(firstEntry.getId(), updatedEntry.getId());
        assertEquals(new CollectionEntry(firstEntry.getId(), 1, 1, 5), foundEntry);
    }

    @Test
    void setCoinQuantity_shouldThrowExceptionWhenQuantityIsNegative() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.setCoinQuantity(1, 1, -1)
        );

        assertEquals("Quantity cannot be negative.", exception.getMessage());
    }

    @Test
    void getOwnedCoinsForUser_shouldReturnOnlyOwnedCoins() {
        service.setCoinQuantity(1, 1, 2);
        service.setCoinQuantity(1, 2, 0);

        List<OwnedCoinView> ownedCoins = service.getOwnedCoinsForUser(1);

        assertEquals(1, ownedCoins.size());
        assertEquals(
                new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2),
                ownedCoins.get(0)
        );
    }

    @Test
    void getMissingCoinsForUser_shouldReturnMissingCoins() {
        service.setCoinQuantity(1, 1, 2);
        service.setCoinQuantity(1, 2, 0);

        List<MissingCoinView> missingCoins = service.getMissingCoinsForUser(1);

        assertEquals(1, missingCoins.size());
        assertEquals(
                new MissingCoinView(2, "Germany", "1 Euro", 2010),
                missingCoins.get(0)
        );
    }

    @Test
    void getOwnedCoinsForUser_shouldFilterByCountry() {
        service.setCoinQuantity(1, 1, 2);
        service.setCoinQuantity(1, 2, 1);

        OwnedCoinFilter filter = new OwnedCoinFilter("Bulgaria", null, null, null, null);

        List<OwnedCoinView> ownedCoins = service.getOwnedCoinsForUser(1, filter);

        assertEquals(1, ownedCoins.size());
        assertEquals(
                new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2),
                ownedCoins.get(0)
        );
    }

    @Test
    void getOwnedCoinsForUser_shouldFilterByMinimumQuantity() {
        service.setCoinQuantity(1, 1, 2);
        service.setCoinQuantity(1, 2, 1);

        OwnedCoinFilter filter = new OwnedCoinFilter(null, null, null, null, 2);

        List<OwnedCoinView> ownedCoins = service.getOwnedCoinsForUser(1, filter);

        assertEquals(1, ownedCoins.size());
        assertEquals(
                new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2),
                ownedCoins.get(0)
        );
    }

    @Test
    void getOwnedCoinsForUser_shouldThrowExceptionWhenYearRangeIsInvalid() {
        OwnedCoinFilter filter = new OwnedCoinFilter(null, null, 2020, 2000, null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getOwnedCoinsForUser(1, filter)
        );

        assertEquals("Minimum year cannot be greater than maximum year.", exception.getMessage());
    }

    @Test
    void getMissingCoinsForUser_shouldFilterByCountry() {
        service.setCoinQuantity(1, 1, 2);
        service.setCoinQuantity(1, 2, 0);

        MissingCoinFilter filter = new MissingCoinFilter("Germany", null, null, null);

        List<MissingCoinView> missingCoins = service.getMissingCoinsForUser(1, filter);

        assertEquals(1, missingCoins.size());
        assertEquals(
                new MissingCoinView(2, "Germany", "1 Euro", 2010),
                missingCoins.get(0)
        );
    }

    @Test
    void getMissingCoinsForUser_shouldFilterByYearRange() {
        service.setCoinQuantity(1, 1, 0);
        service.setCoinQuantity(1, 2, 0);

        MissingCoinFilter filter = new MissingCoinFilter(null, null, 2005, 2015);

        List<MissingCoinView> missingCoins = service.getMissingCoinsForUser(1, filter);

        assertEquals(1, missingCoins.size());
        assertEquals(
                new MissingCoinView(2, "Germany", "1 Euro", 2010),
                missingCoins.get(0)
        );
    }

    @Test
    void getMissingCoinsForUser_shouldThrowExceptionWhenYearRangeIsInvalid() {
        MissingCoinFilter filter = new MissingCoinFilter(null, null, 2020, 2000);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getMissingCoinsForUser(1, filter)
        );

        assertEquals("Minimum year cannot be greater than maximum year.", exception.getMessage());
    }

    @Test
    void getOwnedCoinsForUser_shouldSortByYearDescending() {
        service.setCoinQuantity(1, 1, 2);
        service.setCoinQuantity(1, 2, 1);

        OwnedCoinQuery query = new OwnedCoinQuery(
                null,
                OwnedCoinSortField.YEAR,
                SortDirection.DESC,
                null
        );

        List<OwnedCoinView> ownedCoins = service.getOwnedCoinsForUser(1, query);

        assertEquals(2, ownedCoins.size());
        assertEquals(new OwnedCoinView(2, "Germany", "1 Euro", 2010, 1), ownedCoins.get(0));
        assertEquals(new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2), ownedCoins.get(1));
    }

    @Test
    void getOwnedCoinsForUser_shouldApplyPagination() {
        service.setCoinQuantity(1, 1, 2);
        service.setCoinQuantity(1, 2, 1);

        OwnedCoinQuery query = new OwnedCoinQuery(
                null,
                OwnedCoinSortField.COIN_ID,
                SortDirection.ASC,
                new PageRequest(2, 1)
        );

        List<OwnedCoinView> ownedCoins = service.getOwnedCoinsForUser(1, query);

        assertEquals(1, ownedCoins.size());
        assertEquals(new OwnedCoinView(2, "Germany", "1 Euro", 2010, 1), ownedCoins.get(0));
    }

    @Test
    void getMissingCoinsForUser_shouldSortByYearDescending() {
        service.setCoinQuantity(1, 1, 0);
        service.setCoinQuantity(1, 2, 0);

        MissingCoinQuery query = new MissingCoinQuery(
                null,
                MissingCoinSortField.YEAR,
                SortDirection.DESC,
                null
        );

        List<MissingCoinView> missingCoins = service.getMissingCoinsForUser(1, query);

        assertEquals(2, missingCoins.size());
        assertEquals(new MissingCoinView(2, "Germany", "1 Euro", 2010), missingCoins.get(0));
        assertEquals(new MissingCoinView(1, "Bulgaria", "1 Lev", 2002), missingCoins.get(1));
    }

    @Test
    void getMissingCoinsForUser_shouldApplyPagination() {
        service.setCoinQuantity(1, 1, 0);
        service.setCoinQuantity(1, 2, 0);

        MissingCoinQuery query = new MissingCoinQuery(
                null,
                MissingCoinSortField.COIN_ID,
                SortDirection.ASC,
                new PageRequest(2, 1)
        );

        List<MissingCoinView> missingCoins = service.getMissingCoinsForUser(1, query);

        assertEquals(1, missingCoins.size());
        assertEquals(new MissingCoinView(2, "Germany", "1 Euro", 2010), missingCoins.get(0));
    }

    @Test
    void getOwnedCoinsForUser_shouldThrowExceptionWhenPageNumberIsInvalid() {
        OwnedCoinQuery query = new OwnedCoinQuery(
                null,
                OwnedCoinSortField.COIN_ID,
                SortDirection.ASC,
                new PageRequest(0, 10)
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.getOwnedCoinsForUser(1, query)
        );

        assertEquals("Page number must be greater than 0.", exception.getMessage());
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