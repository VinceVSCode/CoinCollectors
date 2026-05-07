// v0.4.0: Controller tests for collection write endpoints.
package com.vincevscode.cointracker.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.vincevscode.cointracker.model.CollectionEntry;
import com.vincevscode.cointracker.service.CollectionTrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CollectionCommandController.class)
@Import(RestExceptionHandler.class)
class CollectionCommandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollectionTrackingService collectionTrackingService;

    @Test
    void setCoinQuantity_shouldReturnUpdatedCollectionEntry() throws Exception {
        when(collectionTrackingService.setCoinQuantity(1, 2, 3))
                .thenReturn(new CollectionEntry(5, 1, 2, 3));

        String requestBody = """
                {
                  "quantity": 3
                }
                """;

        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
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
    void setCoinQuantity_shouldReturnBadRequestWhenQuantityIsInvalid() throws Exception {
        when(collectionTrackingService.setCoinQuantity(1, 2, -1))
                .thenThrow(new IllegalArgumentException("Quantity cannot be negative."));

        String requestBody = """
                {
                  "quantity": -1
                }
                """;

        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Quantity cannot be negative."));
    }

    @Test
    void setCoinQuantity_shouldReturnBadRequestWhenBodyIsMissing() throws Exception {
        mockMvc.perform(
                        put("/api/users/1/collection/2")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest());
    }
}