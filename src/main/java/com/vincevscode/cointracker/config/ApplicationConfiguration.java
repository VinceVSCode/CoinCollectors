// v0.4.0: Spring configuration for data access and service beans.
package com.vincevscode.cointracker.config;

import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;
import com.vincevscode.cointracker.repository.PostgresCollectionEntryRepository;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import com.vincevscode.cointracker.repository.CoinCatalogQueryRepositoryInterface;
import com.vincevscode.cointracker.repository.PostgresCoinCatalogQueryRepository;
import com.vincevscode.cointracker.service.CoinCatalogQueryService;

import javax.sql.DataSource;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public DataSource dataSource() {
        DatabaseConfig databaseConfig = DatabaseConfig.fromEnvironment();

        HikariDataSource dataSource = new HikariDataSource();
        dataSource.setJdbcUrl(databaseConfig.getUrl());
        dataSource.setUsername(databaseConfig.getUsername());
        dataSource.setPassword(databaseConfig.getPassword());

        return dataSource;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    public CollectionEntryRepositoryInterface collectionEntryRepository(JdbcTemplate jdbcTemplate) {
        return new PostgresCollectionEntryRepository(jdbcTemplate);
    }

    @Bean
    public CollectionTrackingService collectionTrackingService(
            CollectionEntryRepositoryInterface collectionEntryRepository
    ) {
        return new CollectionTrackingService(collectionEntryRepository);
    }

    @Bean
    public CoinCatalogQueryRepositoryInterface coinCatalogQueryRepository(JdbcTemplate jdbcTemplate) {
        return new PostgresCoinCatalogQueryRepository(jdbcTemplate);
    }

    @Bean
    public CoinCatalogQueryService coinCatalogQueryService(
            CoinCatalogQueryRepositoryInterface coinCatalogQueryRepository
    ) {
        return new CoinCatalogQueryService(coinCatalogQueryRepository);
    }
}