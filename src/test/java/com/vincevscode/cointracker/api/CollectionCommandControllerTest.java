// v0.4.0: Controller tests for collection write endpoints.
// v0.6.1: Runs with the real SecurityConfig so @PreAuthorize ownership rules are exercised.
package com.vincevscode.cointracker.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.vincevscode.cointracker.support.AuthTestSupport.asUser;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollectionCommandController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class CollectionCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollectionTrackingService collectionTrackingService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void setCoinQuantity_shouldReturnUpdatedCollectionEntryWhenOwner() throws Exception {
        when(collectionTrackingService.setCoinQuantity(1, 2, 3))
                .thenReturn(new CollectionEntry(5, 1, 2, 3));

        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .with(csrf())
                                .with(asUser(1, UserRole.USER))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\": 3}")
                )
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$.entryId").value(5))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.coinId").value(2))
                .andExpect(jsonPath("$.quantity").value(3));

        verify(collectionTrackingService).setCoinQuantity(1, 2, 3);
    }

    @Test
    void setCoinQuantity_shouldAllowAdminToEditAnotherUsersCollection() throws Exception {
        when(collectionTrackingService.setCoinQuantity(1, 2, 3))
                .thenReturn(new CollectionEntry(5, 1, 2, 3));

        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .with(csrf())
                                .with(asUser(99, UserRole.ADMIN))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\": 3}")
                )
                .andExpect(status().isOk());
    }

    @Test
    void setCoinQuantity_shouldReturnForbiddenWhenEditingAnotherUsersCollection() throws Exception {
        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .with(csrf())
                                .with(asUser(2, UserRole.USER))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\": 3}")
                )
                .andExpect(status().isForbidden());
    }

    @Test
    void setCoinQuantity_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .with(csrf())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\": 3}")
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    void setCoinQuantity_shouldReturnBadRequestWhenQuantityIsInvalid() throws Exception {
        when(collectionTrackingService.setCoinQuantity(1, 2, -1))
                .thenThrow(new IllegalArgumentException("Quantity cannot be negative."));

        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .with(csrf())
                                .with(asUser(1, UserRole.USER))
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"quantity\": -1}")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quantity cannot be negative."));
    }

    @Test
    void setCoinQuantity_shouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .with(csrf())
                                .with(asUser(1, UserRole.USER))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}
