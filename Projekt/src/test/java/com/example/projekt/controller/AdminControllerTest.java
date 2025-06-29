package com.example.projekt.controller;

import com.example.projekt.Entity.User;
import com.example.projekt.config.CustomAuthenticationSuccessHandler;
import com.example.projekt.config.SecurityConfig;
import com.example.projekt.service.UserService;
import com.example.projekt.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@Import(SecurityConfig.class)
class AdminControllerTest {

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
    @WithMockUser(roles = "ADMIN")
    void listUsers_asAdmin_shouldReturnUsersList() throws Exception {
        when(userService.findAllUsers()).thenReturn(Collections.singletonList(new User()));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users-list"))
                .andExpect(model().attributeExists("users"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void listUsers_asEmployee_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isForbidden());
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void deleteUser_asAdmin_shouldRedirect() throws Exception {
        User user = new User("test", "pass", "Test", "test@test.com");
        when(userService.findUserById(1L)).thenReturn(user);

        mockMvc.perform(post("/admin/users/delete/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }
}