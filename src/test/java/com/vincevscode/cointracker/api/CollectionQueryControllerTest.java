// v0.4.0: Controller tests for collection query endpoints.
// v0.6.1: Runs with the real SecurityConfig so @PreAuthorize ownership rules are exercised.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.config.SecurityConfig;
import com.vincevscode.cointracker.model.UserRole;
import com.vincevscode.cointracker.query.MissingCoinQuery;
import com.vincevscode.cointracker.query.OwnedCoinQuery;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import com.vincevscode.cointracker.view.CollectionProgressView;
import com.vincevscode.cointracker.view.MissingCoinView;
import com.vincevscode.cointracker.view.OwnedCoinView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.vincevscode.cointracker.support.AuthTestSupport.asUser;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollectionQueryController.class)
@Import({RestExceptionHandler.class, SecurityConfig.class})
class CollectionQueryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CollectionTrackingService collectionTrackingService;

    // Only needed to satisfy SecurityConfig's UserDetailsService bean dependency in this slice.
    @MockBean
    private AuthUserQueryService authUserQueryService;

    @Test
    void getOwnedCoinsForUser_shouldReturnOwnedCoinsAsJson() throws Exception {
        when(collectionTrackingService.getOwnedCoinsForUser(eq(1), ArgumentMatchers.<OwnedCoinQuery>any()))
                .thenReturn(List.of(
                        new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2),
                        new OwnedCoinView(2, "Germany", "1 Euro", 2010, 1)
                ));

        mockMvc.perform(get("/api/users/1/owned-coins").with(asUser(1, UserRole.USER)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].coinId").value(1))
                .andExpect(jsonPath("$[0].country").value("Bulgaria"))
                .andExpect(jsonPath("$[0].denomination").value("1 Lev"))
                .andExpect(jsonPath("$[0].year").value(2002))
                .andExpect(jsonPath("$[0].quantity").value(2))
                .andExpect(jsonPath("$[1].coinId").value(2))
                .andExpect(jsonPath("$[1].country").value("Germany"));

        verify(collectionTrackingService).getOwnedCoinsForUser(eq(1), ArgumentMatchers.<OwnedCoinQuery>any());
    }

    @Test
    void getOwnedCoinsForUser_shouldAcceptFilterSortAndPagingParameters() throws Exception {
        when(collectionTrackingService.getOwnedCoinsForUser(eq(1), ArgumentMatchers.<OwnedCoinQuery>any()))
                .thenReturn(List.of(
                        new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2)
                ));

        mockMvc.perform(
                        get("/api/users/1/owned-coins")
                                .with(asUser(1, UserRole.USER))
                                .param("country", "Bulgaria")
                                .param("minYear", "2000")
                                .param("maxYear", "2010")
                                .param("minQuantity", "2")
                                .param("sortField", "YEAR")
                                .param("sortDirection", "DESC")
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].coinId").value(1));

        verify(collectionTrackingService).getOwnedCoinsForUser(eq(1), ArgumentMatchers.<OwnedCoinQuery>any());
    }

    @Test
    void getMissingCoinsForUser_shouldReturnMissingCoinsAsJson() throws Exception {
        when(collectionTrackingService.getMissingCoinsForUser(eq(1), ArgumentMatchers.<MissingCoinQuery>any()))
                .thenReturn(List.of(
                        new MissingCoinView(3, "France", "2 Euro", 2015),
                        new MissingCoinView(4, "Italy", "50 Centesimi", 2007)
                ));

        mockMvc.perform(get("/api/users/1/missing-coins").with(asUser(1, UserRole.USER)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].coinId").value(3))
                .andExpect(jsonPath("$[0].country").value("France"))
                .andExpect(jsonPath("$[0].denomination").value("2 Euro"))
                .andExpect(jsonPath("$[0].year").value(2015));

        verify(collectionTrackingService).getMissingCoinsForUser(eq(1), ArgumentMatchers.<MissingCoinQuery>any());
    }

    @Test
    void getOwnedCoinsForUser_shouldAllowAdminToViewAnotherUsersCollection() throws Exception {
        when(collectionTrackingService.getOwnedCoinsForUser(eq(1), ArgumentMatchers.<OwnedCoinQuery>any()))
                .thenReturn(List.of(new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2)));

        mockMvc.perform(get("/api/users/1/owned-coins").with(asUser(99, UserRole.ADMIN)))
                .andExpect(status().isOk());
    }

    @Test
    void getOwnedCoinsForUser_shouldReturnForbiddenWhenViewingAnotherUsersCollection() throws Exception {
        mockMvc.perform(get("/api/users/1/owned-coins").with(asUser(2, UserRole.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOwnedCoinsForUser_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users/1/owned-coins"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getOwnedCoinsForUser_shouldReturnBadRequestWhenPageAndSizeAreIncomplete() throws Exception {
        mockMvc.perform(
                        get("/api/users/1/owned-coins")
                                .with(asUser(1, UserRole.USER))
                                .param("page", "1")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Both page and size must be provided together."));
    }

    @Test
    void getOwnedCoinsForUser_shouldReturnBadRequestWhenServiceThrowsValidationError() throws Exception {
        when(collectionTrackingService.getOwnedCoinsForUser(eq(1), ArgumentMatchers.<OwnedCoinQuery>any()))
                .thenThrow(new IllegalArgumentException("Page number must be greater than 0."));

        mockMvc.perform(
                        get("/api/users/1/owned-coins")
                                .with(asUser(1, UserRole.USER))
                                .param("page", "0")
                                .param("size", "10")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Page number must be greater than 0."));
    }

    @Test
    void getOwnedCoinsForUser_shouldReturnPagedResponseWhenPaginationIsRequested() throws Exception {
        when(collectionTrackingService.getOwnedCoinsForUser(eq(1), ArgumentMatchers.<OwnedCoinQuery>any()))
                .thenReturn(List.of(
                        new OwnedCoinView(1, "Bulgaria", "1 Lev", 2002, 2)
                ));

        when(collectionTrackingService.countOwnedCoinsForUser(eq(1), ArgumentMatchers.<com.vincevscode.cointracker.query.OwnedCoinFilter>any()))
                .thenReturn(7L);

        mockMvc.perform(
                        get("/api/users/1/owned-coins")
                                .with(asUser(1, UserRole.USER))
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].coinId").value(1))
                .andExpect(jsonPath("$.totalCount").value(7))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));
    }

    @Test
    void getCollectionProgress_shouldReturnProgressForOwner() throws Exception {
        when(collectionTrackingService.getCollectionProgress(1))
                .thenReturn(new CollectionProgressView(1, 6, 2, 4, 33.3));

        mockMvc.perform(get("/api/users/1/progress").with(asUser(1, UserRole.USER)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.totalCoinsInCatalog").value(6))
                .andExpect(jsonPath("$.ownedCoinCount").value(2))
                .andExpect(jsonPath("$.missingCoinCount").value(4))
                .andExpect(jsonPath("$.percentageComplete").value(33.3));
    }

    @Test
    void getCollectionProgress_shouldReturnForbiddenForAnotherUser() throws Exception {
        mockMvc.perform(get("/api/users/1/progress").with(asUser(2, UserRole.USER)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getCollectionProgress_shouldReturnUnauthorizedWhenNotLoggedIn() throws Exception {
        mockMvc.perform(get("/api/users/1/progress"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getMissingCoinsForUser_shouldReturnPagedResponseWhenPaginationIsRequested() throws Exception {
        when(collectionTrackingService.getMissingCoinsForUser(eq(1), ArgumentMatchers.<MissingCoinQuery>any()))
                .thenReturn(List.of(
                        new MissingCoinView(3, "France", "2 Euro", 2015)
                ));

        when(collectionTrackingService.countMissingCoinsForUser(eq(1), ArgumentMatchers.<com.vincevscode.cointracker.query.MissingCoinFilter>any()))
                .thenReturn(4L);

        mockMvc.perform(
                        get("/api/users/1/missing-coins")
                                .with(asUser(1, UserRole.USER))
                                .param("page", "2")
                                .param("size", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].coinId").value(3))
                .andExpect(jsonPath("$.totalCount").value(4))
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5));
    }
}
