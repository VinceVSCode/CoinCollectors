// v0.10.0: Unit tests for the audit trail read service's limit handling.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.repository.AdminAuditRepositoryInterface;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.Mockito.verify;

class AdminAuditServiceTest {

    private AdminAuditRepositoryInterface repository;
    private AdminAuditService service;

    @BeforeEach
    void setUp() {
        repository = Mockito.mock(AdminAuditRepositoryInterface.class);
        service = new AdminAuditService(repository);
    }

    @Test
    void getRecentActions_shouldApplyDefaultLimitWhenUnspecified() {
        service.getRecentActions(null);

        verify(repository).getRecentActions(50);
    }

    @Test
    void getRecentActions_shouldApplyDefaultLimitForNonPositiveValues() {
        service.getRecentActions(0);
        service.getRecentActions(-5);

        verify(repository, Mockito.times(2)).getRecentActions(50);
    }

    @Test
    void getRecentActions_shouldPassThroughLimitWithinRange() {
        service.getRecentActions(10);

        verify(repository).getRecentActions(10);
    }

    // Clamped rather than rejected — an oversized limit is imprecision, not an error.
    @Test
    void getRecentActions_shouldClampLimitToMaximum() {
        service.getRecentActions(5000);

        verify(repository).getRecentActions(500);
    }
}
