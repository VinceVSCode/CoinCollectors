// v0.7.1: Controller tests for admin user management endpoints (ADMIN-only, with CSRF).
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.UserManagementService;
import com.vincevscode.cointracker.view.AdminUserView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.vincevscode.cointracker.support.AuthTestSupport.asUser;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminUserController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserManagementService userManagementService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void getUsers_shouldReturnUsersForAdmin() throws Exception {
        when(userManagementService.getAllUsers()).thenReturn(List.of(
                new AdminUserView(1, "vince", UserRole.ADMIN, true),
                new AdminUserView(2, "alex", UserRole.USER, true)
        ));

        mockMvc.perform(get("/api/admin/users").with(asUser(1, UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("vince"))
                .andExpect(jsonPath("$[0].role").value("ADMIN"))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].active").value(true));
    }

    @Test
    void getUsers_shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/users").with(asUser(2, UserRole.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void updateRole_shouldPromoteUserForAdmin() throws Exception {
        when(userManagementService.setUserRole(2, UserRole.ADMIN))
                .thenReturn(new AdminUserView(2, "alex", UserRole.ADMIN, true));

        mockMvc.perform(
                        patch("/api/admin/users/2/role")
                                .with(csrf())
                                .with(asUser(1, UserRole.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\": \"ADMIN\"}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void updateRole_shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(
                        patch("/api/admin/users/2/role")
                                .with(csrf())
                                .with(asUser(2, UserRole.USER))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\": \"ADMIN\"}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void updateRole_shouldReturnBadRequestWhenServiceRejects() throws Exception {
        when(userManagementService.setUserRole(1, UserRole.USER))
                .thenThrow(new IllegalArgumentException("Cannot remove the last active administrator."));

        mockMvc.perform(
                        patch("/api/admin/users/1/role")
                                .with(csrf())
                                .with(asUser(1, UserRole.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"role\": \"USER\"}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Cannot remove the last active administrator."));
    }

    @Test
    void updateActive_shouldDeactivateUserForAdmin() throws Exception {
        when(userManagementService.setUserActive(2, false))
                .thenReturn(new AdminUserView(2, "alex", UserRole.USER, false));

        mockMvc.perform(
                        patch("/api/admin/users/2/active")
                                .with(csrf())
                                .with(asUser(1, UserRole.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"active\": false}")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));
    }
}
