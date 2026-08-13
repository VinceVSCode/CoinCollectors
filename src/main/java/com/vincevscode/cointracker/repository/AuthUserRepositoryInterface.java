// v0.4.3: Repository contract for auth-focused user loading operations.
// v0.7.1: Adds admin management operations (list, update role/active, count admins).
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;

import java.util.List;

/**
 * Storage contract for the credential/role side of a user. {@code findAuthUserByUsername} is
 * the hot path Spring Security calls on every login attempt (via
 * {@link com.vincevscode.cointracker.security.AuthUserDetailsService}); {@code createAuthUser}
 * expects an already-hashed password (hashing happens in
 * {@link com.vincevscode.cointracker.service.UserRegistrationService}, never here).
 * The admin-management methods (getAllAuthUsers/updateRole/updateActive/countActiveAdmins)
 * were added for {@code AdminUserController} — {@code countActiveAdmins} exists specifically
 * so {@link com.vincevscode.cointracker.service.UserManagementService} can refuse to demote
 * or deactivate the last remaining admin.
 */
public interface AuthUserRepositoryInterface {
    AuthUser findAuthUserById(int userId);

    AuthUser findAuthUserByUsername(String username);

    AuthUser createAuthUser(String username, String passwordHash, UserRole role, boolean active);

    List<AuthUser> getAllAuthUsers();

    AuthUser updateRole(int userId, UserRole role);

    AuthUser updateActive(int userId, boolean active);

    /**
     * @return the updated user, or {@code null} if no user has that id.
     */
    AuthUser updatePasswordHash(int userId, String passwordHash);

    long countActiveAdmins();
}
