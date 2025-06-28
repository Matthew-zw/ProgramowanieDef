package com.example.projekt.exception;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ExceptionTest {

    @Test
    void testResourceNotFoundExceptionWithMessage() {
        String message = "Zasób nie istnieje";
        ResourceNotFoundException ex = new ResourceNotFoundException(message);
        assertEquals(message, ex.getMessage());
    }

    @Test
    void testResourceNotFoundExceptionWithDetails() {
        String expectedMessage = "User not found with id : '123'";
        ResourceNotFoundException ex = new ResourceNotFoundException("User", "id", 123L);
        assertEquals(expectedMessage, ex.getMessage());
    }

    @Test
    void testUserAlreadyExistAuthenticationException() {
        String message = "Użytkownik już istnieje";
        UserAlreadyExistAuthenticationException ex = new UserAlreadyExistAuthenticationException(message);
        assertEquals(message, ex.getMessage());
    }
}