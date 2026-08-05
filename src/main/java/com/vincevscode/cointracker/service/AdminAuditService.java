// v0.10.0: Read service for the admin audit trail.
package com.vincevscode.cointracker.service;

import com.vincevscode.cointracker.repository.AdminAuditRepositoryInterface;
import com.vincevscode.cointracker.view.AdminActionView;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Read side of the audit trail. Writes deliberately do not go through here: they happen inside
 * the transaction of the operation being audited (see {@link UserManagementService} and
 * {@link CoinCatalogManagementService}), so a change and its audit record commit together or
 * not at all. A separate write path here would let the two drift apart.
 */
public class AdminAuditService {
    private static final int DEFAULT_LIMIT = 50;
    private static final int MAX_LIMIT = 500;

    private final AdminAuditRepositoryInterface adminAuditRepository;

    public AdminAuditService(AdminAuditRepositoryInterface adminAuditRepository) {
        this.adminAuditRepository = adminAuditRepository;
    }

    @Transactional(readOnly = true)
    public List<AdminActionView> getRecentActions(Integer limit) {
        return adminAuditRepository.getRecentActions(resolveLimit(limit));
    }

    // Clamped rather than rejected: an out-of-range limit is a caller being imprecise about how
    // much history it wants, not an error worth failing the request over.
    private int resolveLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return DEFAULT_LIMIT;
        }

        return Math.min(limit, MAX_LIMIT);
    }
}
