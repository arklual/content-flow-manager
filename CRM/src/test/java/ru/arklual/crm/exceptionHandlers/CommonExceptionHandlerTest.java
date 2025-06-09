package ru.arklual.crm.exceptionHandlers;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.core.MethodParameter;
import ru.arklual.crm.exception.ResourceAlreadyExistsException;
import ru.arklual.crm.exception.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CommonExceptionHandlerTest {

    private final CommonExceptionHandler handler = new CommonExceptionHandler();

    @Test
    void handleResourceNotFound_shouldReturn404() {
        var ex = new ResourceNotFoundException("Not found");
        var response = handler.handleResourceNotFound(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleResourceAlreadyExists_shouldReturn409() {
        var ex = new ResourceAlreadyExistsException("Already exists");
        var response = handler.handleResourceAlreadyExists(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Already exists", response.getBody().getMessage());
    }

    @Test
    void handleValidationErrors_shouldReturn400WithMessage() {
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError error = new FieldError("obj", "email", "must not be blank");
        when(bindingResult.getFieldErrors()).thenReturn(List.of(error));
        var ex = new MethodArgumentNotValidException(mock(MethodParameter.class), bindingResult);

        var response = handler.handleValidationErrors(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(Objects.requireNonNull(response.getBody()).getMessage().contains("email: must not be blank"));
    }

    @Test
    void handleBadCredentials_shouldReturn401() {
        var ex = new BadCredentialsException("Bad credentials");
        var response = handler.handleBadCredentials(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Wrong email or password", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleAccessDenied_shouldReturn403() {
        var ex = new AccessDeniedException("Denied");
        var response = handler.handleAccessDenied(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Access Denied", Objects.requireNonNull(response.getBody()).getMessage());
    }
}