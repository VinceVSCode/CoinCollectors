// v0.4.0: Spring configuration for repository and service beans.
package com.vincevscode.cointracker.config;

import com.vincevscode.cointracker.repository.CollectionEntryRepositoryInterface;
import com.vincevscode.cointracker.repository.PostgresCollectionEntryRepository;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public CollectionEntryRepositoryInterface collectionEntryRepository() {
        return new PostgresCollectionEntryRepository();
    }

    @Bean
    public CollectionTrackingService collectionTrackingService(
            CollectionEntryRepositoryInterface collectionEntryRepository
    ) {
        return new CollectionTrackingService(collectionEntryRepository);
    }
}