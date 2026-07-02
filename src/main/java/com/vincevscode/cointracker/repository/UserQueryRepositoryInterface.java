// v0.4.3: Repository contract for user query operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.view.UserView;

import java.util.List;

public interface UserQueryRepositoryInterface {
    List<UserView> getUsers();

    UserView getUserById(int userId);
}