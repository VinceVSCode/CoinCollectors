// v0.2.5: Factory for creating repository implementations from environment configuration.
package com.vincevscode.cointracker.config;

import com.vincevscode.cointracker.repository.CoinRepositoryInterface;
import com.vincevscode.cointracker.repository.InMemoryCoinRepository;
import com.vincevscode.cointracker.repository.PostgresCoinRepository;

public class RepositoryFactory {

    private RepositoryFactory() {
    }

    public static CoinRepositoryInterface createRepository() {
        String repositoryType = System.getenv("COIN_TRACKER_REPOSITORY");

        if (repositoryType == null || repositoryType.isBlank()) {
            System.out.println("COIN_TRACKER_REPOSITORY not set. Defaulting to in-memory repository.");
            return new InMemoryCoinRepository();
        }

        if (repositoryType.equalsIgnoreCase("postgres")) {
            System.out.println("Using PostgreSQL repository.");
            return new PostgresCoinRepository();
        }

        if (repositoryType.equalsIgnoreCase("memory")) {
            System.out.println("Using in-memory repository.");
            return new InMemoryCoinRepository();
        }

        throw new IllegalStateException(
                "Unsupported repository type: " + repositoryType +
                        ". Use 'memory' or 'postgres'."
        );
    }
}