// v0.3.0: Repository contract for user storage operations.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.User;

import java.util.List;

public interface UserRepositoryInterface {
    void addUser(User user);

    List<User> getAllUsers();

    User findUserById(int id);
}