package com.example.projekt.controller;

import com.example.projekt.dto.VerifyTotpCodeDto;
import com.example.projekt.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class TwoFactorAuthenticationController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;

    @GetMapping("/verify-2fa")
    public String showVerify2faForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("TEMP_AUTHENTICATED_USER") == null) {
            return "redirect:/login";
        }
        model.addAttribute("verifyTotpCodeDto", new VerifyTotpCodeDto());
        return "auth/verify-2fa";
    }

    @PostMapping("/perform_verify_2fa")
    public String verify2faCode(@ModelAttribute("verifyTotpCodeDto") @Valid VerifyTotpCodeDto verifyDto,
                                BindingResult result,
                                HttpServletRequest request,
                                Model model) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "redirect:/login";
        }
        String username = (String) session.getAttribute("TEMP_AUTHENTICATED_USER");
        if (username == null) {
            return "redirect:/login";
        }

        if (result.hasErrors()) {
            return "auth/verify-2fa";
        }

        if (userService.verifyTotpCodeForUser(username, verifyDto.getCode())) {
            session.removeAttribute("TEMP_AUTHENTICATED_USER");
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            return "redirect:/projects";
        } else {
            model.addAttribute("errorMessage", "Nieprawidłowy kod uwierzytelniający.");
            return "auth/verify-2fa";
        }
    }
}