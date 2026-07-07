// v0.5.1: Unit tests for AuthUserDetails' mapping of AuthUser onto Spring Security's UserDetails contract.
package com.vincevscode.cointracker.security;

import com.vincevscode.cointracker.model.AuthUser;
import com.vincevscode.cointracker.model.UserRole;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.LocalDateTime;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthUserDetailsTest {

    @Test
    void shouldExposeUnderlyingUserFields() {
        AuthUser authUser = new AuthUser(1, "vince", "hash", UserRole.ADMIN, true, LocalDateTime.now());
        AuthUserDetails userDetails = new AuthUserDetails(authUser);

        assertEquals(1, userDetails.getUserId());
        assertEquals(UserRole.ADMIN, userDetails.getRole());
        assertEquals("vince", userDetails.getUsername());
        assertEquals("hash", userDetails.getPassword());
    }

    @Test
    void getAuthorities_shouldMapRoleToSpringSecurityAuthority() {
        AuthUser adminUser = new AuthUser(1, "vince", "hash", UserRole.ADMIN, true, LocalDateTime.now());
        AuthUserDetails adminDetails = new AuthUserDetails(adminUser);

        Collection<? extends GrantedAuthority> authorities = adminDetails.getAuthorities();

        assertEquals(1, authorities.size());
        assertEquals("ROLE_ADMIN", authorities.iterator().next().getAuthority());
    }

    @Test
    void isEnabledAndAccountNonLocked_shouldReflectActiveFlag() {
        AuthUser inactiveUser = new AuthUser(2, "alex", "hash", UserRole.USER, false, LocalDateTime.now());
        AuthUserDetails userDetails = new AuthUserDetails(inactiveUser);

        assertFalse(userDetails.isEnabled());
        assertFalse(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isCredentialsNonExpired());
    }
}
