// v0.9.0: Controller tests for admin coin catalog endpoints (ADMIN-only, with CSRF).
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.CoinCatalogManagementService;
import com.vincevscode.cointracker.view.CoinCatalogView;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.vincevscode.cointracker.support.AuthTestSupport.asUser;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Runs the REAL filter chain (see AdminUserControllerTest's note) so the class-level
// ADMIN-only @PreAuthorize and CSRF enforcement are genuinely exercised.
@WebMvcTest(AdminCoinController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class AdminCoinControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoinCatalogManagementService coinCatalogManagementService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void createCoin_shouldReturnCreatedCoinForAdmin() throws Exception {
        when(coinCatalogManagementService.createCoin("Bulgaria", "1 Lev", 2002))
                .thenReturn(new CoinCatalogView(7, "Bulgaria", "1 Lev", 2002));

        mockMvc.perform(post("/api/admin/coins")
                        .with(asUser(1, UserRole.ADMIN))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"Bulgaria\",\"denomination\":\"1 Lev\",\"year\":2002}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.coinId").value(7))
                .andExpect(jsonPath("$.country").value("Bulgaria"));
    }

    @Test
    void createCoin_shouldReturnBadRequestWhenServiceRejectsInput() throws Exception {
        when(coinCatalogManagementService.createCoin(anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Year must be between 1 and 2999."));

        mockMvc.perform(post("/api/admin/coins")
                        .with(asUser(1, UserRole.ADMIN))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"Bulgaria\",\"denomination\":\"1 Lev\",\"year\":99999}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Year must be between 1 and 2999."));
    }

    @Test
    void createCoin_shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(post("/api/admin/coins")
                        .with(asUser(2, UserRole.USER))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"Bulgaria\",\"denomination\":\"1 Lev\",\"year\":2002}"))
                .andExpect(status().isForbidden());

        verify(coinCatalogManagementService, never()).createCoin(anyString(), anyString(), anyInt());
    }

    @Test
    void createCoin_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(post("/api/admin/coins")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"Bulgaria\",\"denomination\":\"1 Lev\",\"year\":2002}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createCoin_shouldReturnForbiddenWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/api/admin/coins")
                        .with(asUser(1, UserRole.ADMIN))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"Bulgaria\",\"denomination\":\"1 Lev\",\"year\":2002}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateCoin_shouldReturnUpdatedCoinForAdmin() throws Exception {
        when(coinCatalogManagementService.updateCoin(1, "Germany", "2 Euro", 2011))
                .thenReturn(new CoinCatalogView(1, "Germany", "2 Euro", 2011));

        mockMvc.perform(put("/api/admin/coins/1")
                        .with(asUser(1, UserRole.ADMIN))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"Germany\",\"denomination\":\"2 Euro\",\"year\":2011}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.denomination").value("2 Euro"));
    }

    @Test
    void updateCoin_shouldReturnBadRequestForUnknownCoin() throws Exception {
        when(coinCatalogManagementService.updateCoin(anyInt(), anyString(), anyString(), anyInt()))
                .thenThrow(new IllegalArgumentException("Coin was not found."));

        mockMvc.perform(put("/api/admin/coins/99")
                        .with(asUser(1, UserRole.ADMIN))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"country\":\"Germany\",\"denomination\":\"2 Euro\",\"year\":2011}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Coin was not found."));
    }

    @Test
    void deleteCoin_shouldReturnNoContentAndDefaultToNonForcedDelete() throws Exception {
        mockMvc.perform(delete("/api/admin/coins/1")
                        .with(asUser(1, UserRole.ADMIN))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // The destructive path must be opt-in: an unqualified DELETE has to arrive as force=false.
        verify(coinCatalogManagementService).deleteCoin(1, false);
    }

    @Test
    void deleteCoin_shouldPassForceFlagThrough() throws Exception {
        mockMvc.perform(delete("/api/admin/coins/1?force=true")
                        .with(asUser(1, UserRole.ADMIN))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        verify(coinCatalogManagementService).deleteCoin(1, true);
    }

    @Test
    void deleteCoin_shouldSurfaceCascadeRefusalAsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException(
                "This coin is in 3 collection entries, which will be deleted with it. Confirm to delete anyway."))
                .when(coinCatalogManagementService).deleteCoin(eq(1), eq(false));

        mockMvc.perform(delete("/api/admin/coins/1")
                        .with(asUser(1, UserRole.ADMIN))
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(
                        "This coin is in 3 collection entries, which will be deleted with it. Confirm to delete anyway."));
    }

    @Test
    void deleteCoin_shouldReturnForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(delete("/api/admin/coins/1")
                        .with(asUser(2, UserRole.USER))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        verify(coinCatalogManagementService, never()).deleteCoin(anyInt(), Mockito.anyBoolean());
    }

    @Test
    void deleteCoin_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(delete("/api/admin/coins/1").with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
