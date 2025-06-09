package com.example.projekt.controller;

import com.example.projekt.dto.UserRegistrationDto;
import com.example.projekt.exception.UserAlreadyExistAuthenticationException;
import com.example.projekt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;


@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegistrationController {

    private final UserService userService;

    @ModelAttribute("userRegistrationDto")
    public UserRegistrationDto userRegistrationDto() {
        return new UserRegistrationDto();
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        return "register";
    }

    @PostMapping
    public String registerUserAccount(@Valid @ModelAttribute("userRegistrationDto") UserRegistrationDto registrationDto,
                                      BindingResult result,
                                      RedirectAttributes redirectAttributes) {

        if (userService.usernameExists(registrationDto.getUsername())) {
            result.addError(new FieldError("userRegistrationDto", "username", "Nazwa użytkownika jest już zajęta."));
        }
        if (userService.emailExists(registrationDto.getEmail())) {
            result.addError(new FieldError("userRegistrationDto", "email", "Adres email jest już zajęty."));
        }


        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.registerNewUser(registrationDto);
            redirectAttributes.addFlashAttribute("registrationSuccess", "Rejestracja zakończona pomyślnie! Możesz się teraz zalogować.");
            return "redirect:/login";
        } catch (UserAlreadyExistAuthenticationException e) {
            result.reject("user.exists.general", e.getMessage());
            return "register";
        }
    }
}