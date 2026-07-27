// v0.4.3: Supported application user roles for authorization groundwork.
package com.vincevscode.cointracker.model;

/**
 * Authorization tiers checked by {@code @PreAuthorize} expressions across the API layer
 * (e.g. {@code hasRole('ADMIN')}). USER can only act on their own collection; ADMIN can
 * act on any user's data and manage accounts via {@code AdminUserController}.
 * Note: a role change only takes effect on the user's NEXT login, since Spring Security
 * snapshots granted authorities into the session at login time (see
 * {@link com.vincevscode.cointracker.security.AuthUserDetailsService}).
 */
public enum UserRole {
    USER,
    ADMIN
}