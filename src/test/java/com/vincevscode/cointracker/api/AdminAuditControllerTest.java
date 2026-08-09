// v0.10.0: Controller tests for the admin audit trail endpoint (ADMIN-only).
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AdminAuditService;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.view.AdminActionView;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static com.vincevscode.cointracker.support.AuthTestSupport.asUser;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Runs the REAL filter chain so the ADMIN-only @PreAuthorize is genuinely exercised.
@WebMvcTest(AdminAuditController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class AdminAuditControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminAuditService adminAuditService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void getRecentActions_shouldReturnTrailForAdmin() throws Exception {
        when(adminAuditService.getRecentActions(null)).thenReturn(List.of(
                new AdminActionView(
                        2, 1, "vince", "COIN_DELETED", "COIN", 5,
                        "Spain 20 Centesimos (2003); cascaded 1 collection entry",
                        LocalDateTime.of(2026, 8, 3, 12, 30)
                ),
                new AdminActionView(
                        1, 1, "vince", "USER_ROLE_CHANGED", "USER", 2,
                        "alex: USER -> ADMIN",
                        LocalDateTime.of(2026, 8, 3, 12, 0)
                )
        ));

        mockMvc.perform(get("/api/admin/audit").with(asUser(1, UserRole.ADMIN)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].action").value("COIN_DELETED"))
                .andExpect(jsonPath("$[0].actorUsername").value("vince"))
                .andExpect(jsonPath("$[0].targetType").value("COIN"))
                .andExpect(jsonPath("$[0].targetId").value(5))
                .andExpect(jsonPath("$[0].details")
                        .value("Spain 20 Centesimos (2003); cascaded 1 collection entry"))
                .andExpect(jsonPath("$[1].action").value("USER_ROLE_CHANGED"));
    }

    @Test
    void getRecentActions_shouldPassLimitThrough() throws Exception {
        when(adminAuditService.getRecentActions(10)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/audit?limit=10").with(asUser(1, UserRole.ADMIN)))
                .andExpect(status().isOk());

        verify(adminAuditService).getRecentActions(10);
    }

    @Test
    void getRecentActions_shouldTreatMissingLimitAsUnspecified() throws Exception {
        when(adminAuditService.getRecentActions(null)).thenReturn(List.of());

        mockMvc.perform(get("/api/admin/audit").with(asUser(1, UserRole.ADMIN)))
                .andExpect(status().isOk());

        verify(adminAuditService).getRecentActions(isNull());
    }

    @Test
    void getRecentActions_shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/api/admin/audit").with(asUser(2, UserRole.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getRecentActions_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/admin/audit"))
                .andExpect(status().isUnauthorized());
    }
}
