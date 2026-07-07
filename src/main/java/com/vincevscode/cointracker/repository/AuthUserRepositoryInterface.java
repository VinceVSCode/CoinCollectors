// v0.4.3: Repository contract for auth-focused user loading operations.
// v0.7.1: Adds admin management operations (list, update role/active, count admins).
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;

import java.util.List;

public interface AuthUserRepositoryInterface {
    AuthUser findAuthUserById(int userId);

    AuthUser findAuthUserByUsername(String username);

    AuthUser createAuthUser(String username, String passwordHash, UserRole role, boolean active);

    List<AuthUser> getAllAuthUsers();

    AuthUser updateRole(int userId, UserRole role);

    AuthUser updateActive(int userId, boolean active);

    long countActiveAdmins();
}
