package ru.arklual.crm.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import ru.arklual.crm.entity.User;
import ru.arklual.crm.entity.UserStatus;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsTest {

    @Test
    void shouldExposeUserFieldsAndAuthoritiesCorrectly() {
        User mockUser = mock(User.class);
        when(mockUser.getEmail()).thenReturn("user@example.com");
        when(mockUser.getPasswordHash()).thenReturn("hashed_password");
        when(mockUser.getStatus()).thenReturn(UserStatus.ACTIVE);

        CustomUserDetails userDetails = new CustomUserDetails(mockUser);

        assertEquals("user@example.com", userDetails.getUsername());
        assertEquals("hashed_password", userDetails.getPassword());

        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
        assertEquals(1, authorities.size());
        assertTrue(authorities.iterator().next().getAuthority().contains("ROLE_USER"));

        assertTrue(userDetails.isAccountNonExpired());
        assertTrue(userDetails.isAccountNonLocked());
        assertTrue(userDetails.isCredentialsNonExpired());
        assertTrue(userDetails.isEnabled());
    }

    @Test
    void shouldBeDisabledIfUserIsInactive() {
        User mockUser = mock(User.class);
        when(mockUser.getStatus()).thenReturn(UserStatus.INACTIVE);
        CustomUserDetails userDetails = new CustomUserDetails(mockUser);
        assertFalse(userDetails.isAccountNonExpired());
        assertFalse(userDetails.isAccountNonLocked());
        assertFalse(userDetails.isCredentialsNonExpired());
        assertFalse(userDetails.isEnabled());
    }
}