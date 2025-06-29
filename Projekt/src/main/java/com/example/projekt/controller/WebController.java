package com.example.projekt.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WebController {
    /**
     *
     * @return
     */
    @GetMapping("/")
    public String index() {
        return "/login";
    }

    /**
     *
     * @param error
     * @param logout
     * @param model
     * @return
     */
    @GetMapping("/login")
    public String loginPage(@RequestParam(value = "error", required = false) String error,
                            @RequestParam(value = "logout", required = false) String logout,
                            Model model) {
        if (error != null) {
            model.addAttribute("loginError", "Nieprawidłowa nazwa użytkownika lub hasło.");
        }
        if (logout != null) {
            model.addAttribute("logoutMessage", "Zostałeś pomyślnie wylogowany.");
        }
        return "login";
    }
}
