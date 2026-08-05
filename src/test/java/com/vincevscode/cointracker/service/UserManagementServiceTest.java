// v0.7.1: Unit tests for UserManagementService admin operations and the last-admin safety guard.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.model.AdminActionType;
import com.vincevscode.cointracker.model.AdminActor;
import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.repository.AdminAuditRepositoryInterface;
import com.vincevscode.cointracker.repository.AuthUserRepositoryInterface;
import com.vincevscode.cointracker.view.AdminUserView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class UserManagementServiceTest {
    private static final AdminActor ACTOR = new AdminActor(1, "vince");

    private AuthUserRepositoryInterface repository;
    private AdminAuditRepositoryInterface auditRepository;
    private UserManagementService service;

    @BeforeEach
    void setUp() {
        repository = mock(AuthUserRepositoryInterface.class);
        auditRepository = mock(AdminAuditRepositoryInterface.class);
        service = new UserManagementService(repository, auditRepository);
    }

    private AuthUser user(int id, String username, UserRole role, boolean active) {
        return new AuthUser(id, username, "hash", role, active, LocalDateTime.now());
    }

    @Test
    void getAllUsers_shouldMapRepositoryUsersToViews() {
        when(repository.getAllAuthUsers()).thenReturn(List.of(
                user(1, "vince", UserRole.ADMIN, true),
                user(2, "alex", UserRole.USER, true)
        ));

        List<AdminUserView> users = service.getAllUsers();

        assertEquals(2, users.size());
        assertEquals(new AdminUserView(1, "vince", UserRole.ADMIN, true), users.get(0));
        assertEquals(new AdminUserView(2, "alex", UserRole.USER, true), users.get(1));
    }

    @Test
    void setUserRole_shouldPromoteUserToAdmin() {
        when(repository.findAuthUserById(2)).thenReturn(user(2, "alex", UserRole.USER, true));
        when(repository.updateRole(2, UserRole.ADMIN)).thenReturn(user(2, "alex", UserRole.ADMIN, true));

        AdminUserView result = service.setUserRole(ACTOR, 2, UserRole.ADMIN);

        assertEquals(new AdminUserView(2, "alex", UserRole.ADMIN, true), result);
        verify(auditRepository).recordAction(
                ACTOR, AdminActionType.USER_ROLE_CHANGED, "USER", 2, "alex: USER -> ADMIN");
    }

    @Test
    void setUserRole_shouldDemoteAdminWhenAnotherActiveAdminExists() {
        when(repository.findAuthUserById(1)).thenReturn(user(1, "vince", UserRole.ADMIN, true));
        when(repository.countActiveAdmins()).thenReturn(2L);
        when(repository.updateRole(1, UserRole.USER)).thenReturn(user(1, "vince", UserRole.USER, true));

        AdminUserView result = service.setUserRole(ACTOR, 1, UserRole.USER);

        assertEquals(UserRole.USER, result.getRole());
    }

    @Test
    void setUserRole_shouldRejectDemotingTheLastActiveAdmin() {
        when(repository.findAuthUserById(1)).thenReturn(user(1, "vince", UserRole.ADMIN, true));
        when(repository.countActiveAdmins()).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.setUserRole(ACTOR, 1, UserRole.USER)
        );

        assertEquals("Cannot remove the last active administrator.", exception.getMessage());
        verify(repository, never()).updateRole(1, UserRole.USER);
        // A refused change is not a change: nothing may be written to the trail for it.
        verifyNoInteractions(auditRepository);
    }

    @Test
    void setUserRole_shouldRejectUnknownUser() {
        when(repository.findAuthUserById(99)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.setUserRole(ACTOR, 99, UserRole.USER)
        );

        assertEquals("User was not found.", exception.getMessage());
    }

    @Test
    void setUserActive_shouldDeactivateRegularUser() {
        when(repository.findAuthUserById(2)).thenReturn(user(2, "alex", UserRole.USER, true));
        when(repository.updateActive(2, false)).thenReturn(user(2, "alex", UserRole.USER, false));

        AdminUserView result = service.setUserActive(ACTOR, 2, false);

        assertEquals(new AdminUserView(2, "alex", UserRole.USER, false), result);
        verify(auditRepository).recordAction(
                ACTOR, AdminActionType.USER_DEACTIVATED, "USER", 2, "alex");
    }

    @Test
    void setUserActive_shouldRecordActivationSeparatelyFromDeactivation() {
        when(repository.findAuthUserById(2)).thenReturn(user(2, "alex", UserRole.USER, false));
        when(repository.updateActive(2, true)).thenReturn(user(2, "alex", UserRole.USER, true));

        service.setUserActive(ACTOR, 2, true);

        verify(auditRepository).recordAction(
                ACTOR, AdminActionType.USER_ACTIVATED, "USER", 2, "alex");
    }

    @Test
    void setUserRole_shouldRejectMissingActor() {
        assertEquals(
                "Acting administrator is required.",
                assertThrows(
                        IllegalArgumentException.class,
                        () -> service.setUserRole(null, 2, UserRole.ADMIN)
                ).getMessage()
        );

        verifyNoInteractions(auditRepository);
    }

    @Test
    void setUserActive_shouldRejectDeactivatingTheLastActiveAdmin() {
        when(repository.findAuthUserById(1)).thenReturn(user(1, "vince", UserRole.ADMIN, true));
        when(repository.countActiveAdmins()).thenReturn(1L);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> service.setUserActive(ACTOR, 1, false)
        );

        assertEquals("Cannot deactivate the last active administrator.", exception.getMessage());
        verify(repository, never()).updateActive(1, false);
    }

    @Test
    void setUserActive_shouldRejectInvalidUserId() {
        assertThrows(
                IllegalArgumentException.class,
                () -> service.setUserActive(ACTOR, 0, true)
        );
    }
}
