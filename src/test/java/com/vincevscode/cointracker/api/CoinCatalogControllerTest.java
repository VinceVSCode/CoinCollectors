// v0.4.2: Controller tests for catalog coin endpoints.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.query.CoinCatalogFilter;
import com.vincevscode.cointracker.query.CoinCatalogQuery;
import com.vincevscode.cointracker.service.CoinCatalogQueryService;
import com.vincevscode.cointracker.view.CoinCatalogView;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CoinCatalogController.class)
@Import(RestExceptionHandler.class)
class CoinCatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CoinCatalogQueryService coinCatalogQueryService;

    @Test
    void getCoins_shouldReturnCatalogCoinsAsJson() throws Exception {
        when(coinCatalogQueryService.getCoins(ArgumentMatchers.<CoinCatalogQuery>any()))
                .thenReturn(List.of(
                        new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002),
                        new CoinCatalogView(2, "Germany", "1 Euro", 2010)
                ));

        mockMvc.perform(get("/api/coins"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("application/json"))
                .andExpect(jsonPath("$[0].coinId").value(1))
                .andExpect(jsonPath("$[0].country").value("Bulgaria"))
                .andExpect(jsonPath("$[1].coinId").value(2))
                .andExpect(jsonPath("$[1].country").value("Germany"));

        verify(coinCatalogQueryService).getCoins(ArgumentMatchers.<CoinCatalogQuery>any());
    }

    @Test
    void getCoins_shouldAcceptFilterSortAndPagingParameters() throws Exception {
        when(coinCatalogQueryService.getCoins(ArgumentMatchers.<CoinCatalogQuery>any()))
                .thenReturn(List.of(
                        new CoinCatalogView(1, "Bulgaria", "1 Lev", 2002)
                ));

        when(coinCatalogQueryService.countCoins(ArgumentMatchers.<CoinCatalogFilter>any()))
                .thenReturn(5L);

        mockMvc.perform(
                        get("/api/coins")
                                .param("country", "Bulgaria")
                                .param("minYear", "2000")
                                .param("maxYear", "2010")
                                .param("sortField", "YEAR")
                                .param("sortDirection", "DESC")
                                .param("page", "1")
                                .param("size", "10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].coinId").value(1))
                .andExpect(jsonPath("$.totalCount").value(5))
                .andExpect(jsonPath("$.pageNumber").value(1))
                .andExpect(jsonPath("$.pageSize").value(10));

        verify(coinCatalogQueryService).getCoins(ArgumentMatchers.<CoinCatalogQuery>any());
        verify(coinCatalogQueryService).countCoins(ArgumentMatchers.<CoinCatalogFilter>any());
    }

    @Test
    void getCoins_shouldReturnBadRequestWhenPageAndSizeAreIncomplete() throws Exception {
        mockMvc.perform(
                        get("/api/coins")
                                .param("page", "1")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Both page and size must be provided together."));
    }

    @Test
    void getCoins_shouldReturnBadRequestWhenServiceThrowsValidationError() throws Exception {
        when(coinCatalogQueryService.getCoins(ArgumentMatchers.<CoinCatalogQuery>any()))
                .thenThrow(new IllegalArgumentException("Minimum year cannot be greater than maximum year."));

        mockMvc.perform(
                        get("/api/coins")
                                .param("minYear", "2020")
                                .param("maxYear", "2000")
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Minimum year cannot be greater than maximum year."));
    }
}