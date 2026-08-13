// v0.12.0: Request DTO for a self-service password change.
package com.vincevscode.cointracker.api.dto;

/**
 * Body shape for {@code PATCH /api/auth/password}.
 *
 * <p>Carries no {@code username} or {@code userId}: the account being changed is taken from the
 * authenticated principal, so there is no field here a caller could repoint at someone else's
 * account — the same allowlisting reasoning as {@link RegisterRequest}, which omits role/active.
 */
public class ChangePasswordRequest {
    private String currentPassword;
    private String newPassword;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() {
        return currentPassword;
    }

    public void setCurrentPassword(String currentPassword) {
        this.currentPassword = currentPassword;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
}
