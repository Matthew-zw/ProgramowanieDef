package com.example.projekt.config;

import com.example.projekt.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DataInitializerTest {

    @Autowired
    private UserRepository userRepository;


    @Test
    void run_shouldInitializeDefaultUsers() {
        assertTrue(userRepository.findByUsername("admin").isPresent());
        assertTrue(userRepository.findByUsername("manager").isPresent());
        assertTrue(userRepository.findByUsername("user").isPresent());
    }
}