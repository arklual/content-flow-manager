package ru.arklual.crm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.AuthenticationException;
import ru.arklual.crm.dto.responses.StatusResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class CRMAuthenticationEntryPointTest {

    @Test
    void commence_shouldReturnUnauthorizedJsonResponse() throws Exception {
        CRMAuthenticationEntryPoint entryPoint = new CRMAuthenticationEntryPoint();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException exception = mock(AuthenticationException.class);

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(printWriter);

        entryPoint.commence(request, response, exception);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        printWriter.flush();
        String json = stringWriter.toString();

        StatusResponse result = new ObjectMapper().readValue(json, StatusResponse.class);
        assertEquals("Authentication Failed", result.getStatus());
    }
}