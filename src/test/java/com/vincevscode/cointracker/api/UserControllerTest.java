// v0.4.3: Controller tests for user endpoints.
// v0.6.1: Runs with the real SecurityConfig; list is ADMIN-only, get-by-id is self-or-admin.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.UserQueryService;
import com.vincevscode.cointracker.view.UserView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.vincevscode.cointracker.support.AuthTestSupport.asUser;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserQueryService userQueryService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void getUsers_shouldReturnUsersAsJsonForAdmin() throws Exception {
        when(userQueryService.getUsers()).thenReturn(List.of(
                new UserView(1, "vince"),
                new UserView(2, "alex")
        ));

        mockMvc.perform(get("/api/users").with(asUser(1, UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1))
                .andExpect(jsonPath("$[0].username").value("vince"))
                .andExpect(jsonPath("$[1].userId").value(2))
                .andExpect(jsonPath("$[1].username").value("alex"));
    }

    @Test
    void getUsers_shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/users").with(asUser(2, UserRole.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUsers_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getUserById_shouldReturnUserAsJsonForSelf() throws Exception {
        when(userQueryService.getUserById(eq(1))).thenReturn(new UserView(1, "vince"));

        mockMvc.perform(get("/api/users/1").with(asUser(1, UserRole.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.username").value("vince"));

        verify(userQueryService).getUserById(1);
    }

    @Test
    void getUserById_shouldReturnUserAsJsonForAdmin() throws Exception {
        when(userQueryService.getUserById(eq(1))).thenReturn(new UserView(1, "vince"));

        mockMvc.perform(get("/api/users/1").with(asUser(99, UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getUserById_shouldReturnForbiddenForAnotherUser() throws Exception {
        mockMvc.perform(get("/api/users/1").with(asUser(2, UserRole.USER)))
                .andExpect(status().isForbidden());
    }
}
