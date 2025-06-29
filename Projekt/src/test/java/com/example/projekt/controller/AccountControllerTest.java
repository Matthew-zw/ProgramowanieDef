package com.example.projekt.controller;

import com.example.projekt.config.CustomAuthenticationSuccessHandler;
import com.example.projekt.config.SecurityConfig;
import com.example.projekt.dto.TwoFactorSetupDto;
import com.example.projekt.service.UserService;
import com.example.projekt.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@Import(SecurityConfig.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser")
    void showAccountSettings_shouldReturnSettingsView() throws Exception {
        TwoFactorSetupDto dto = new TwoFactorSetupDto();
        dto.setEnabled(false);
        when(userService.setupTwoFactorAuthentication(anyString())).thenReturn(dto);

        mockMvc.perform(get("/account/settings"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings"))
                .andExpect(model().attributeExists("twoFactorSetup", "verifyCodeDto"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    void showAccountSettings_unauthenticated_shouldRedirectToLogin() throws Exception {
        mockMvc.perform(get("/account/settings"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("http://localhost/login"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser")
    void disableTwoFactor_shouldRedirectToSettings() throws Exception {
        mockMvc.perform(post("/account/disable-2fa").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/settings"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser")
    void enableTwoFactor_withValidCode_shouldRedirectAndShowSuccess() throws Exception {
        when(userService.enableTwoFactorAuthentication("testuser", "123456")).thenReturn(true);

        mockMvc.perform(post("/account/enable-2fa").with(csrf())
                        .param("code", "123456"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/account/settings"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(username = "testuser")
    void enableTwoFactor_withInvalidCode_shouldReturnViewWithError() throws Exception {
        when(userService.enableTwoFactorAuthentication("testuser", "000000")).thenReturn(false);
        when(userService.setupTwoFactorAuthentication("testuser")).thenReturn(new TwoFactorSetupDto());

        mockMvc.perform(post("/account/enable-2fa").with(csrf())
                        .param("code", "000000"))
                .andExpect(status().isOk())
                .andExpect(view().name("account/settings"))
                .andExpect(model().attributeExists("twoFactorError"));
    }
}