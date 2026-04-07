// v0.2.6: Supported repository types for application startup configuration.
package com.vincevscode.cointracker.config;

public enum RepositoryType {
    MEMORY,
    POSTGRES;

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

        throw new IllegalStateException(
                "Unsupported repository type: " + value +
                        ". Use 'memory' or 'postgres'."
        );
    }
}