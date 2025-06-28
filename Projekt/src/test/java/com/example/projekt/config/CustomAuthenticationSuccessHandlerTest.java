package com.example.projekt.config;

import com.example.projekt.Entity.User;
import com.example.projekt.repository.UserRepository;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomAuthenticationSuccessHandlerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomAuthenticationSuccessHandler successHandler;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }

    @Test
    void onAuthenticationSuccess_forAdmin_redirectsToAdminUsers() throws Exception {
        when(authentication.getName()).thenReturn("admin");

        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(authorities).when(authentication).getAuthorities();

        User adminUser = new User();
        adminUser.setTwoFactorEnabled(false);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/admin/users", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccess_forUser_redirectsToProjects() throws Exception {
        when(authentication.getName()).thenReturn("user");

        Collection<? extends GrantedAuthority> authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_EMPLOYEE"));
        doReturn(authorities).when(authentication).getAuthorities();

        User regularUser = new User();
        regularUser.setTwoFactorEnabled(false);
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/projects", response.getRedirectedUrl());
    }

    @Test
    void onAuthenticationSuccess_with2FAEnabled_redirectsToVerify2fa() throws Exception {
        when(authentication.getName()).thenReturn("user2fa");

        User userWith2FA = new User();
        userWith2FA.setTwoFactorEnabled(true);
        when(userRepository.findByUsername("user2fa")).thenReturn(Optional.of(userWith2FA));

        successHandler.onAuthenticationSuccess(request, response, authentication);

        assertEquals("/verify-2fa", response.getRedirectedUrl());
        assertEquals("user2fa", request.getSession().getAttribute("TEMP_AUTHENTICATED_USER"));
    }
}