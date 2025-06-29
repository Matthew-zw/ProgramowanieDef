package com.example.projekt.config;

import com.example.projekt.Entity.User;
import com.example.projekt.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;

    /**
     *
     * @param request
     * @param response
     * @param authentication
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user != null && user.isTwoFactorEnabled()) {
            HttpSession session = request.getSession();
            session.setAttribute("TEMP_AUTHENTICATED_USER", username);
            response.sendRedirect(request.getContextPath() + "/verify-2fa");
        } else {

            boolean isAdmin = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(role -> role.equals("ROLE_ADMIN"));

            if (isAdmin) {
                response.sendRedirect(request.getContextPath() + "/admin/users");
            } else {
                response.sendRedirect(request.getContextPath() + "/projects");
            }
        }
    }
}