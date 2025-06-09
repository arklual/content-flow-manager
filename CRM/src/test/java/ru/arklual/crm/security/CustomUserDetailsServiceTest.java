package ru.arklual.crm.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.arklual.crm.entity.User;
import ru.arklual.crm.entity.UserStatus;
import ru.arklual.crm.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CustomUserDetailsServiceTest {

    @Test
    void loadUserByUsername_shouldReturnUserDetails_ifUserExists() {
        UserRepository userRepository = mock(UserRepository.class);
        User mockUser = new User();
        mockUser.setEmail("user@example.com");
        mockUser.setPasswordHash("password");
        mockUser.setStatus(UserStatus.ACTIVE);

        when(userRepository.findByEmail("user@example.com"))
                .thenReturn(Optional.of(mockUser));

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

        var result = service.loadUserByUsername("user@example.com");

        assertNotNull(result);
        assertEquals("user@example.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_shouldThrow_ifUserNotFound() {
        UserRepository userRepository = mock(UserRepository.class);
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        CustomUserDetailsService service = new CustomUserDetailsService(userRepository);

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("missing@example.com"));
    }
}