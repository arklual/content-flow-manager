package ru.arklual.crm.service;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.arklual.crm.dto.requests.RegisterRequest;
import ru.arklual.crm.entity.User;
import ru.arklual.crm.entity.UserStatus;
import ru.arklual.crm.exception.ResourceAlreadyExistsException;
import ru.arklual.crm.repository.UserRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthServiceTest {

    @Test
    void register_shouldSaveUser_whenEmailNotExists() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthService authService = new AuthService(userRepository, passwordEncoder);

        RegisterRequest request = new RegisterRequest("tralala@example.com", "Tralaleila Tralala", "password");

        when(userRepository.findByEmail("tralala@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");

        authService.register(request);

        verify(userRepository).save(argThat(user ->
                user.getEmail().equals("tralala@example.com") &&
                        user.getName().equals("Tralaleila Tralala") &&
                        user.getPasswordHash().equals("encodedPassword") &&
                        user.getStatus() == UserStatus.ACTIVE
        ));
    }

    @Test
    void register_shouldThrow_whenEmailAlreadyExists() {
        UserRepository userRepository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthService authService = new AuthService(userRepository, passwordEncoder);

        RegisterRequest request = new RegisterRequest("tralala@example.com", "Tralaleila Tralala", "password");

        when(userRepository.findByEmail("tralala@example.com"))
                .thenReturn(Optional.of(new User()));

        assertThrows(ResourceAlreadyExistsException.class, () -> authService.register(request));
    }
}