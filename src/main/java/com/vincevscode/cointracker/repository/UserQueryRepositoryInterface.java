// v0.4.3: Repository contract for user query operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.view.UserView;

import java.util.List;

/**
 * Read-side contract backing {@code GET /api/users} and {@code GET /api/users/{id}} — returns
 * the display-safe {@link UserView} rather than a domain model, so it never risks leaking
 * password hashes even if a future field gets added to {@link User}/{@link com.vincevscode.cointracker.model.AuthUser}.
 */
public interface UserQueryRepositoryInterface {
    List<UserView> getUsers();

    UserView getUserById(int userId);
}