// v0.3.0: Repository contract for user storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.User;

import java.util.List;

/**
 * Minimal write/lookup contract for the non-auth {@link User} projection. Deliberately thin
 * (no update/delete) since user creation now goes through auth registration
 * ({@link AuthUserRepositoryInterface#createAuthUser}) — this interface mainly exists for
 * legacy/tests that only care about id+username, not credentials.
 */
public interface UserRepositoryInterface {
    void addUser(User user);

    List<User> getAllUsers();

    User findUserById(int id);
}