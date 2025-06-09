package ru.arklual.crm.exceptionHandlers;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

class GrpcExceptionHandlerTest {

    private final GrpcExceptionHandler handler = new GrpcExceptionHandler();

    @Test
    void handleStatusRuntimeException_NOT_FOUND() {
        var ex = new StatusRuntimeException(Status.NOT_FOUND.withDescription("Not found"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Not found", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleStatusRuntimeException_INVALID_ARGUMENT() {
        var ex = new StatusRuntimeException(Status.INVALID_ARGUMENT.withDescription("Invalid input"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Invalid input", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleStatusRuntimeException_ALREADY_EXISTS() {
        var ex = new StatusRuntimeException(Status.ALREADY_EXISTS.withDescription("Already exists"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("Already exists", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleStatusRuntimeException_PERMISSION_DENIED() {
        var ex = new StatusRuntimeException(Status.PERMISSION_DENIED.withDescription("No permission"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("No permission", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleStatusRuntimeException_UNAUTHENTICATED() {
        var ex = new StatusRuntimeException(Status.UNAUTHENTICATED.withDescription("Auth required"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Auth required", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleStatusRuntimeException_UNAVAILABLE() {
        var ex = new StatusRuntimeException(Status.UNAVAILABLE.withDescription("Service down"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertEquals("Service down", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleStatusRuntimeException_UNKNOWN() {
        var ex = new StatusRuntimeException(Status.UNKNOWN.withDescription("Unexpected error"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("Unexpected error", Objects.requireNonNull(response.getBody()).getMessage());
    }

    @Test
    void handleStatusRuntimeException_DEADLINE_EXCEEDED() {
        var ex = new StatusRuntimeException(Status.DEADLINE_EXCEEDED.withDescription("Timeout"));
        var response = handler.handleStatusRuntimeException(ex);

        assertEquals(HttpStatus.GATEWAY_TIMEOUT, response.getStatusCode());
        assertEquals("Timeout", Objects.requireNonNull(response.getBody()).getMessage());
    }

}