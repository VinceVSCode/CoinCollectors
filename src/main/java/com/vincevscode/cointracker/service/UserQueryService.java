// v0.4.3: Service layer for user query operations.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.repository.UserQueryRepositoryInterface;
import com.vincevscode.cointracker.view.UserView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class UserQueryService {
    private final UserQueryRepositoryInterface userQueryRepository;

    public UserQueryService(UserQueryRepositoryInterface userQueryRepository) {
        this.userQueryRepository = userQueryRepository;
    }

    @Transactional(readOnly = true)
    public List<UserView> getUsers() {
        return userQueryRepository.getUsers();
    }

    @Transactional(readOnly = true)
    public UserView getUserById(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }

        return userQueryRepository.getUserById(userId);
    }
}