package ru.arklual.crm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() throws Exception {
        jwtTokenProvider = new JwtTokenProvider();
        Field secretKeyField = JwtTokenProvider.class.getDeclaredField("secretKeyString");
        secretKeyField.setAccessible(true);
        secretKeyField.set(jwtTokenProvider, "d1037745-ba69-4117-aa13-f66e168edd85");
        Field expirationField = JwtTokenProvider.class.getDeclaredField("validityInMilliseconds");
        expirationField.setAccessible(true);
        expirationField.set(jwtTokenProvider, 3600000);
        jwtTokenProvider.init();
    }

    @Test
    void createAndValidateToken_shouldWorkCorrectly() {
        String email = "user@example.com";
        String token = jwtTokenProvider.createToken(email);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
    }

    @Test
    void validateToken_shouldFailForInvalidToken() {
        String invalidToken = "invalid.jwt.token";
        assertFalse(jwtTokenProvider.validateToken(invalidToken));
    }
}