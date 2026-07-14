// v0.5.1: Loads AuthUserDetails for Spring Security from the existing AuthUserQueryService.
package com.vincevscode.cointracker.security;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * The bridge Spring Security calls into on every login attempt. Deliberately reuses
 * {@link AuthUserQueryService} rather than talking to the repository directly, so validation
 * and any future business rules around lookup stay in one place. This is also where the
 * "role changes apply on next login" behavior originates: whatever role is on the AuthUser
 * fetched here gets baked into {@link AuthUserDetails#getAuthorities()} for the whole session.
 */
public class AuthUserDetailsService implements UserDetailsService {
    private final AuthUserQueryService authUserQueryService;

    public AuthUserDetailsService(AuthUserQueryService authUserQueryService) {
        this.authUserQueryService = authUserQueryService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AuthUser authUser = authUserQueryService.findAuthUserByUsername(username);

        if (authUser == null) {
            throw new UsernameNotFoundException("No user found with username: " + username);
        }

        return new AuthUserDetails(authUser);
    }
}
