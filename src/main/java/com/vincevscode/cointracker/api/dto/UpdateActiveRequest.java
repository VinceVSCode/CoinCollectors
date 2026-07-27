// v0.7.1: Request DTO for activating/deactivating a user (admin operation).
package com.vincevscode.cointracker.api.dto;

public class UpdateActiveRequest {
    // Boolean (boxed), not boolean: lets the controller distinguish "active omitted from the
    // JSON body" (null -> reject with a clear error) from "explicitly set to false".
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
