package ru.arklual.crm.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import ru.arklual.crm.dto.responses.MessageResponse;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CRMAccessDeniedHandlerTest {

    @Test
    void handle_shouldReturnUnauthorizedJsonResponse() throws Exception {
        CRMAccessDeniedHandler handler = new CRMAccessDeniedHandler();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AccessDeniedException exception = new AccessDeniedException("Access denied");

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(response.getWriter()).thenReturn(printWriter);

        handler.handle(request, response, exception);

        verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        verify(response).setContentType("application/json");

        printWriter.flush();
        String json = stringWriter.toString();

        MessageResponse result = new ObjectMapper().readValue(json, MessageResponse.class);
        assertEquals("Authorization Failed", result.getMessage());
    }
}