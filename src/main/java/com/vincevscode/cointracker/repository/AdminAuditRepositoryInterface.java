// v0.10.0: Persistence contract for the admin audit trail.
package com.vincevscode.cointracker.repository;

import com.vincevscode.cointracker.model.AdminActionType;
import com.vincevscode.cointracker.model.AdminActor;
import com.vincevscode.cointracker.view.AdminActionView;

import java.util.List;

/**
 * Append-only by design: there is no update or delete here, because an audit trail an admin can
 * edit is not evidence of anything.
 */
public interface AdminAuditRepositoryInterface {

    void recordAction(
            AdminActor actor,
            AdminActionType action,
            String targetType,
            Integer targetId,
            String details
    );

    /**
     * @return the most recent actions first, capped at {@code limit}.
     */
    List<AdminActionView> getRecentActions(int limit);
}
