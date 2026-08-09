// v0.10.0: The privileged mutations recorded in the admin audit trail.
package com.vincevscode.cointracker.model;

/**
 * Stored as its {@code name()} in {@code admin_actions.action}. The column is TEXT rather than a
 * DB enum so adding a value here doesn't require a migration; the trade-off is that an old row
 * can name an action this enum no longer has, which is why reads tolerate unknown values instead
 * of failing (see {@code PostgresAdminAuditRepository}).
 */
public enum AdminActionType {
    USER_ROLE_CHANGED,
    USER_ACTIVATED,
    USER_DEACTIVATED,
    COIN_CREATED,
    COIN_UPDATED,
    COIN_DELETED
}
