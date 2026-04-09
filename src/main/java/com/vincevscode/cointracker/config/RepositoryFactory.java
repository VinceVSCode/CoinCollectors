// v0.2.7: Factory for creating repository implementations from environment configuration.
package com.vincevscode.cointracker.config;

import com.vincevscode.cointracker.repository.CachedCoinRepository;
import com.vincevscode.cointracker.repository.CoinRepositoryInterface;
import com.vincevscode.cointracker.repository.InMemoryCoinRepository;
import com.vincevscode.cointracker.repository.PostgresCoinRepository;

public class RepositoryFactory {

    private RepositoryFactory() {
    }

    public static CoinRepositoryInterface createRepository() {
        String repositoryTypeValue = System.getenv("COIN_TRACKER_REPOSITORY");
        RepositoryType repositoryType = RepositoryType.fromEnvironmentValue(repositoryTypeValue);

        switch (repositoryType) {
            case POSTGRES:
                System.out.println("Using PostgreSQL repository.");
                return new PostgresCoinRepository();

            case MEMORY:
                if (repositoryTypeValue == null || repositoryTypeValue.isBlank()) {
                    System.out.println("COIN_TRACKER_REPOSITORY not set. Defaulting to in-memory repository.");
                } else {
                    System.out.println("Using in-memory repository.");
                }
                return new InMemoryCoinRepository();

            case CACHED_MEMORY:
                System.out.println("Using cached in-memory repository.");
                return new CachedCoinRepository(new InMemoryCoinRepository());

            case CACHED_POSTGRES:
                System.out.println("Using cached PostgreSQL repository.");
                return new CachedCoinRepository(new PostgresCoinRepository());

            default:
                throw new IllegalStateException("Unhandled repository type: " + repositoryType);
        }
    }
}