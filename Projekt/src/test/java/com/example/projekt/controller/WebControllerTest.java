package com.example.projekt.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
// --- NOWY IMPORT ---
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// --- KLUCZOWA ZMIANA: Wyłączamy auto-konfigurację security w nowoczesny sposób ---
@WebMvcTest(controllers = WebController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class})
class WebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void index_shouldReturnLoginView() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("/login"));
    }

    @Test
    void loginPage_withError_shouldAddLoginErrorAttribute() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("loginError"));
    }

    @Test
    void loginPage_withLogout_shouldAddLogoutMessageAttribute() throws Exception {
        mockMvc.perform(get("/login").param("logout", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeExists("logoutMessage"));
    }

    @Test
    void loginPage_withNoParams_shouldNotAddAttributes() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"))
                .andExpect(model().attributeDoesNotExist("loginError"))
                .andExpect(model().attributeDoesNotExist("logoutMessage"));
    }
}