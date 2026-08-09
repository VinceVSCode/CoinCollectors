// v0.10.0: Admin-only REST controller exposing the audit trail.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.service.AdminAuditService;
import com.vincevscode.cointracker.view.AdminActionView;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Read-only by design — there is deliberately no endpoint to edit or clear the trail, since an
 * audit log an admin can rewrite is worthless as a record. ADMIN-only via the class-level
 * {@code @PreAuthorize}, matching the other admin controllers.
 */
@RestController
@RequestMapping("/api/admin/audit")
@PreAuthorize("hasRole('ADMIN')")
public class AdminAuditController {
    private final AdminAuditService adminAuditService;

    public AdminAuditController(AdminAuditService adminAuditService) {
        this.adminAuditService = adminAuditService;
    }

    @GetMapping
    public List<AdminActionView> getRecentActions(
            @RequestParam(name = "limit", required = false) Integer limit
    ) {
        return adminAuditService.getRecentActions(limit);
    }
}
