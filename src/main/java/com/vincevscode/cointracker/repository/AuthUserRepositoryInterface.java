// v0.4.3: Repository contract for auth-focused user loading operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;

public interface AuthUserRepositoryInterface {
    AuthUser findAuthUserById(int userId);

    AuthUser findAuthUserByUsername(String username);

    AuthUser createAuthUser(String username, String passwordHash, UserRole role, boolean active);
}