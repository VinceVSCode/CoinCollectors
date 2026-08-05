// v0.10.0: View model for a row of the admin audit trail.
package com.vincevscode.cointracker.view;

import java.time.LocalDateTime;
import java.util.Objects;

// Row shape for GET /api/admin/audit. `action` is the raw stored string rather than the enum so
// a record written by an older/newer build still renders instead of failing to deserialize.
public class AdminActionView {
    private final long actionId;
    private final int actorUserId;
    private final String actorUsername;
    private final String action;
    private final String targetType;
    private final Integer targetId;
    private final String details;
    private final LocalDateTime createdAt;

    public AdminActionView(
            long actionId,
            int actorUserId,
            String actorUsername,
            String action,
            String targetType,
            Integer targetId,
            String details,
            LocalDateTime createdAt
    ) {
        this.actionId = actionId;
        this.actorUserId = actorUserId;
        this.actorUsername = actorUsername;
        this.action = action;
        this.targetType = targetType;
        this.targetId = targetId;
        this.details = details;
        this.createdAt = createdAt;
    }

    public long getActionId() {
        return actionId;
    }

    public int getActorUserId() {
        return actorUserId;
    }

    public String getActorUsername() {
        return actorUsername;
    }

    public String getAction() {
        return action;
    }

    public String getTargetType() {
        return targetType;
    }

    public Integer getTargetId() {
        return targetId;
    }

    public String getDetails() {
        return details;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "AdminActionView{" +
                "actionId=" + actionId +
                ", actorUsername='" + actorUsername + '\'' +
                ", action='" + action + '\'' +
                ", targetType='" + targetType + '\'' +
                ", targetId=" + targetId +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object otherObject) {
        if (this == otherObject) {
            return true;
        }

        if (otherObject == null || getClass() != otherObject.getClass()) {
            return false;
        }

        AdminActionView otherView = (AdminActionView) otherObject;

        return actionId == otherView.actionId
                && actorUserId == otherView.actorUserId
                && Objects.equals(actorUsername, otherView.actorUsername)
                && Objects.equals(action, otherView.action)
                && Objects.equals(targetType, otherView.targetType)
                && Objects.equals(targetId, otherView.targetId)
                && Objects.equals(details, otherView.details)
                && Objects.equals(createdAt, otherView.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId, actorUserId, actorUsername, action, targetType, targetId, details, createdAt);
    }
}
