// v0.2.7: Supported repository types for application startup configuration.
package com.vincevscode.cointracker.config;

public enum RepositoryType {
    MEMORY,
    POSTGRES,
    CACHED_MEMORY,
    CACHED_POSTGRES;

    public static RepositoryType fromEnvironmentValue(String value) {
        if (value == null || value.isBlank()) {
            return MEMORY;
        }

        if (value.equalsIgnoreCase("memory")) {
            return MEMORY;
        }

        if (value.equalsIgnoreCase("postgres")) {
            return POSTGRES;
        }

        if (value.equalsIgnoreCase("cached-memory")) {
            return CACHED_MEMORY;
        }

        if (value.equalsIgnoreCase("cached-postgres")) {
            return CACHED_POSTGRES;
        }

        throw new IllegalStateException(
                "Unsupported repository type: " + value +
                        ". Use 'memory', 'postgres', 'cached-memory', or 'cached-postgres'."
        );
    }
}