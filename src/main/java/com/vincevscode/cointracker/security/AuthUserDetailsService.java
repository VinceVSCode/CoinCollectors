// v0.5.1: Loads AuthUserDetails for Spring Security from the existing AuthUserQueryService.
package com.vincevscode.cointracker.security;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.service.AuthUserQueryService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

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
