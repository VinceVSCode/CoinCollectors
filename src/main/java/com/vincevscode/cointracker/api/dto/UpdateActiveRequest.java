// v0.7.1: Request DTO for activating/deactivating a user (admin operation).
package com.vincevscode.cointracker.api.dto;

public class UpdateActiveRequest {
    private Boolean active;

    public UpdateActiveRequest() {
    }

    public UpdateActiveRequest(Boolean active) {
        this.active = active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
}
