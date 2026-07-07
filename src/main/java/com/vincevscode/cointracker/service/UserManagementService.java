// v0.7.1: Service layer for admin user management (list, change role, activate/deactivate).
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import com.vincevscode.cointracker.view.AdminUserView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class UserManagementService {
    private final AuthUserRepositoryInterface authUserRepository;

    public UserManagementService(AuthUserRepositoryInterface authUserRepository) {
        this.authUserRepository = authUserRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserView> getAllUsers() {
        return authUserRepository.getAllAuthUsers().stream()
                .map(UserManagementService::toView)
                .toList();
    }

    @Transactional
    public AdminUserView setUserRole(int userId, UserRole role) {
        validateUserId(userId);

        if (role == null) {
            throw new IllegalArgumentException("Role is required.");
        }

        AuthUser existing = requireUser(userId);

        boolean removesLastAdmin = existing.getRole() == UserRole.ADMIN
                && existing.isActive()
                && role != UserRole.ADMIN
                && authUserRepository.countActiveAdmins() <= 1;

        if (removesLastAdmin) {
            throw new IllegalArgumentException("Cannot remove the last active administrator.");
        }

        return toView(authUserRepository.updateRole(userId, role));
    }

    @Transactional
    public AdminUserView setUserActive(int userId, boolean active) {
        validateUserId(userId);

        AuthUser existing = requireUser(userId);

        boolean deactivatesLastAdmin = !active
                && existing.getRole() == UserRole.ADMIN
                && existing.isActive()
                && authUserRepository.countActiveAdmins() <= 1;

        if (deactivatesLastAdmin) {
            throw new IllegalArgumentException("Cannot deactivate the last active administrator.");
        }

        return toView(authUserRepository.updateActive(userId, active));
    }

    private AuthUser requireUser(int userId) {
        AuthUser user = authUserRepository.findAuthUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User was not found.");
        }

        return user;
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0.");
        }
    }

    private static AdminUserView toView(AuthUser user) {
        return new AdminUserView(user.getId(), user.getUsername(), user.getRole(), user.isActive());
    }
}
