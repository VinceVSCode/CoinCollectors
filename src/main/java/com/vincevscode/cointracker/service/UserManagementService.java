// v0.7.1: Service layer for admin user management (list, change role, activate/deactivate).
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AdminActionType;
import com.vincevscode.cointracker.model.AdminActor;
import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AdminAuditRepositoryInterface;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import com.vincevscode.cointracker.view.AdminUserView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Backs the ADMIN-only user management endpoints ({@code AdminUserController}): listing users,
 * changing roles, activating/deactivating accounts. The load-bearing rule here is the
 * last-active-admin guard in setUserRole/setUserActive — without it, an admin could lock
 * every admin (including themselves) out of the admin panel with no way back in.
 * Note: a role change here only takes effect on the affected user's NEXT login (Spring
 * Security snapshots authorities into the session at login); deactivation, by contrast, is
 * enforced immediately by {@link com.vincevscode.cointracker.security.AccountStatusFilter}.
 */
public class UserManagementService {
    private static final String TARGET_TYPE_USER = "USER";

    private final AuthUserRepositoryInterface authUserRepository;
    private final AdminAuditRepositoryInterface adminAuditRepository;

    public UserManagementService(
            AuthUserRepositoryInterface authUserRepository,
            AdminAuditRepositoryInterface adminAuditRepository
    ) {
        this.authUserRepository = authUserRepository;
        this.adminAuditRepository = adminAuditRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminUserView> getAllUsers() {
        return authUserRepository.getAllAuthUsers().stream()
                .map(UserManagementService::toView)
                .toList();
    }

    @Transactional
    public AdminUserView setUserRole(AdminActor actor, int userId, UserRole role) {
        requireActor(actor);
        validateUserId(userId);

        if (role == null) {
            throw new IllegalArgumentException("Role is required.");
        }

        AuthUser existing = requireUser(userId);

        // Only blocks the change if THIS user is themselves one of the (<=1) remaining active
        // admins being demoted away from ADMIN — demoting a non-admin or a role no-op is fine.
        boolean removesLastAdmin = existing.getRole() == UserRole.ADMIN
                && existing.isActive()
                && role != UserRole.ADMIN
                && authUserRepository.countActiveAdmins() <= 1;

        if (removesLastAdmin) {
            throw new IllegalArgumentException("Cannot remove the last active administrator.");
        }

        AdminUserView updated = toView(authUserRepository.updateRole(userId, role));

        // Recorded inside this @Transactional method so the change and its audit entry commit
        // together — an audit trail that can silently miss entries proves nothing.
        adminAuditRepository.recordAction(
                actor,
                AdminActionType.USER_ROLE_CHANGED,
                TARGET_TYPE_USER,
                userId,
                existing.getUsername() + ": " + existing.getRole() + " -> " + role
        );

        return updated;
    }

    @Transactional
    public AdminUserView setUserActive(AdminActor actor, int userId, boolean active) {
        requireActor(actor);
        validateUserId(userId);

        AuthUser existing = requireUser(userId);

        boolean deactivatesLastAdmin = !active
                && existing.getRole() == UserRole.ADMIN
                && existing.isActive()
                && authUserRepository.countActiveAdmins() <= 1;

        if (deactivatesLastAdmin) {
            throw new IllegalArgumentException("Cannot deactivate the last active administrator.");
        }

        AdminUserView updated = toView(authUserRepository.updateActive(userId, active));

        adminAuditRepository.recordAction(
                actor,
                active ? AdminActionType.USER_ACTIVATED : AdminActionType.USER_DEACTIVATED,
                TARGET_TYPE_USER,
                userId,
                existing.getUsername()
        );

        return updated;
    }

    private void requireActor(AdminActor actor) {
        if (actor == null) {
            throw new IllegalArgumentException("Acting administrator is required.");
        }
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
