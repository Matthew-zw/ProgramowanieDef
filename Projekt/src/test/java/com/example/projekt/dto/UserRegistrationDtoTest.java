package com.example.projekt.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class UserRegistrationDtoTest {
    @Test
    void userRegistrationDto_passwordConfirmed() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        assertTrue(dto.isPasswordConfirmed());
    }

    @Test
    void userRegistrationDto_passwordNotConfirmed() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setPassword("password123");
        dto.setConfirmPassword("different");
        assertFalse(dto.isPasswordConfirmed());
    }

    @Test
    void userRegistrationDto_passwordNull() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setPassword(null);
        dto.setConfirmPassword("password123");
        assertFalse(dto.isPasswordConfirmed());
    }
}
