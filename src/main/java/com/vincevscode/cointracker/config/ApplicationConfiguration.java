// v0.4.0: Spring configuration for data access and service beans.
package com.vincevscode.cointracker.config;

import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;
import com.vincevscode.cointracker.repository.PostgresCollectionEntryRepository;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import com.vincevscode.cointracker.repository.AdminAuditRepositoryInterface;
import com.vincevscode.cointracker.repository.PostgresAdminAuditRepository;
import com.vincevscode.cointracker.service.AdminAuditService;
import com.vincevscode.cointracker.repository.CoinCatalogCommandRepositoryInterface;
import com.vincevscode.cointracker.repository.CoinCatalogQueryRepositoryInterface;
import com.vincevscode.cointracker.repository.PostgresCoinCatalogCommandRepository;
import com.vincevscode.cointracker.repository.PostgresCoinCatalogQueryRepository;
import com.vincevscode.cointracker.service.CoinCatalogManagementService;
import com.vincevscode.cointracker.service.CoinCatalogQueryService;
import com.vincevscode.cointracker.repository.PostgresUserQueryRepository;
import com.vincevscode.cointracker.repository.UserQueryRepositoryInterface;
import com.vincevscode.cointracker.service.UserQueryService;
import javax.sql.DataSource;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import com.vincevscode.cointracker.repository.PostgresAuthUserRepository;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.UserManagementService;
import com.vincevscode.cointracker.service.UserRegistrationService;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Central bean wiring for the data-access + service layers: one HikariCP DataSource, one
 * shared JdbcTemplate, and a repository+service bean pair per read/write concern
 * (collection tracking, catalog browsing, user queries, auth). Deliberately does NOT wire
 * {@link com.vincevscode.cointracker.repository.CoinRepositoryInterface} or
 * {@link com.vincevscode.cointracker.service.CoinCatalogService} — those are the older,
 * pre-Spring-Boot CRUD path (see that interface's doc) and aren't part of the live app.
 */
@Configuration
public class ApplicationConfiguration {

    @Bean
    public DataSource dataSource() {
        // Config (URL/credentials) comes from required env vars, not application.properties —
        // keeps secrets out of source control; see docker-compose.yml for the COIN_TRACKER_DB_*
        // vars this expects, and DatabaseConfig for the fail-fast validation if any are missing.
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

    @Bean
    public CoinCatalogCommandRepositoryInterface coinCatalogCommandRepository(JdbcTemplate jdbcTemplate) {
        return new PostgresCoinCatalogCommandRepository(jdbcTemplate);
    }

    @Bean
    public CoinCatalogManagementService coinCatalogManagementService(
            CoinCatalogCommandRepositoryInterface coinCatalogCommandRepository,
            AdminAuditRepositoryInterface adminAuditRepository
    ) {
        return new CoinCatalogManagementService(coinCatalogCommandRepository, adminAuditRepository);
    }

    @Bean
    public AdminAuditRepositoryInterface adminAuditRepository(JdbcTemplate jdbcTemplate) {
        return new PostgresAdminAuditRepository(jdbcTemplate);
    }

    @Bean
    public AdminAuditService adminAuditService(AdminAuditRepositoryInterface adminAuditRepository) {
        return new AdminAuditService(adminAuditRepository);
    }

    @Bean
    public UserQueryRepositoryInterface userQueryRepository(JdbcTemplate jdbcTemplate) {
        return new PostgresUserQueryRepository(jdbcTemplate);
    }

    @Bean
    public UserQueryService userQueryService(UserQueryRepositoryInterface userQueryRepository) {
        return new UserQueryService(userQueryRepository);
    }

    @Bean
    public AuthUserRepositoryInterface authUserRepository(JdbcTemplate jdbcTemplate) {
        return new PostgresAuthUserRepository(jdbcTemplate);
    }

    @Bean
    public AuthUserQueryService authUserQueryService(AuthUserRepositoryInterface authUserRepository) {
        return new AuthUserQueryService(authUserRepository);
    }

    @Bean
    public UserRegistrationService userRegistrationService(
            AuthUserRepositoryInterface authUserRepository,
            PasswordEncoder passwordEncoder
    ) {
        return new UserRegistrationService(authUserRepository, passwordEncoder);
    }

    @Bean
    public UserManagementService userManagementService(
            AuthUserRepositoryInterface authUserRepository,
            AdminAuditRepositoryInterface adminAuditRepository
    ) {
        return new UserManagementService(authUserRepository, adminAuditRepository);
    }
}