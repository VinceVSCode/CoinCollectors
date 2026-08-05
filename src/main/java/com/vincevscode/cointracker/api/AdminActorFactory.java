// v0.10.0: Adapts a Spring Security Authentication into the AdminActor the services audit with.
package com.vincevscode.cointracker.api;

import com.vincevscode.cointracker.model.AdminActor;
import com.vincevscode.cointracker.security.AuthUserDetails;
import org.springframework.security.core.Authentication;

/**
 * One place where {@code Authentication} is unwrapped for auditing, so the admin controllers
 * don't each repeat the cast and the service layer never sees a Spring Security type.
 *
 * <p>Reaching here with no authentication would mean the class-level
 * {@code @PreAuthorize("hasRole('ADMIN')")} didn't run, so that case throws rather than
 * recording an action against an unknown actor.
 */
final class AdminActorFactory {

    private AdminActorFactory() {
    }

    static AdminActor fromAuthentication(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthUserDetails principal)) {
            throw new IllegalStateException("An authenticated administrator is required.");
        }

        return new AdminActor(principal.getUserId(), principal.getUsername());
    }
}
