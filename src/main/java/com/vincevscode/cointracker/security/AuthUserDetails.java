// v0.5.1: Adapts AuthUser to Spring Security's UserDetails contract.
package com.vincevscode.cointracker.security;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Spring Security's view of an {@link AuthUser} — wraps rather than modifies the domain model,
 * so nothing outside the security package needs to know about {@code UserDetails}. This is
 * also the type stashed as {@code authentication.principal} that {@code @PreAuthorize}
 * expressions (e.g. {@code #userId == authentication.principal.userId}) read from.
 */
public class AuthUserDetails implements UserDetails {
    private final AuthUser authUser;

    public AuthUserDetails(AuthUser authUser) {
        this.authUser = authUser;
    }

    public int getUserId() {
        return authUser.getId();
    }

    public UserRole getRole() {
        return authUser.getRole();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // "ROLE_" prefix is a Spring Security convention hasRole('ADMIN') strips back off when
        // matching — required for hasRole(...)/hasAnyRole(...) expressions to work.
        return List.of(new SimpleGrantedAuthority("ROLE_" + authUser.getRole().name()));
    }

    @Override
    public String getPassword() {
        return authUser.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return authUser.getUsername();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return authUser.isActive();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // Both isAccountNonLocked and isEnabled key off the same `active` flag — Spring Security
    // checks both but we only have one concept of "deactivated", so they stay in lockstep.
    // Note this only blocks NEW logins/session-establishment; an already-logged-in session
    // isn't affected until AccountStatusFilter re-checks it on the next request.
    @Override
    public boolean isEnabled() {
        return authUser.isActive();
    }
}
