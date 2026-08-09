// v0.7.1: Admin-only REST controller for user management (list, change role, activate/deactivate).
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.api.dto.UpdateActiveRequest;
import com.vincevscode.cointracker.api.dto.UpdateRoleRequest;
import com.vincevscode.cointracker.service.UserManagementService;
import com.vincevscode.cointracker.view.AdminUserView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Class-level {@code @PreAuthorize} means every method here is ADMIN-only by default —
 * no per-method authorization annotation needed unless a future endpoint wants a different
 * rule. Delegates every mutation's business rule (e.g. can't strand the app with zero admins)
 * to {@link UserManagementService}; this controller only validates request shape.
 */
@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final UserManagementService userManagementService;

    public AdminUserController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping
    public List<AdminUserView> getUsers() {
        return userManagementService.getAllUsers();
    }

    @PatchMapping("/{userId}/role")
    public AdminUserView updateRole(
            @PathVariable("userId") int userId,
            @RequestBody UpdateRoleRequest request,
            Authentication authentication
    ) {
        if (request == null || request.getRole() == null) {
            throw new IllegalArgumentException("Role is required.");
        }

        return userManagementService.setUserRole(
                AdminActorFactory.fromAuthentication(authentication),
                userId,
                request.getRole()
        );
    }

    @PatchMapping("/{userId}/active")
    public AdminUserView updateActive(
            @PathVariable("userId") int userId,
            @RequestBody UpdateActiveRequest request,
            Authentication authentication
    ) {
        if (request == null || request.getActive() == null) {
            throw new IllegalArgumentException("Active flag is required.");
        }

        return userManagementService.setUserActive(
                AdminActorFactory.fromAuthentication(authentication),
                userId,
                request.getActive()
        );
    }
}
