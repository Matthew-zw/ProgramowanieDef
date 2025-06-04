package com.example.projekt.controller;


import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.dto.UserRoleUpdateDto;
import com.example.projekt.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ROLE_ADMIN')") // Zabezpieczenie całego kontrolera dla roli ADMIN
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // Wyświetlanie listy wszystkich użytkowników
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users-list"; // Widok: templates/admin/users-list.html
    }

    // Wyświetlanie formularza edycji ról dla konkretnego użytkownika
    @GetMapping("/users/edit-roles/{userId}")
    public String showEditUserRolesForm(@PathVariable Long userId, Model model) {
        User user = userService.findUserById(userId);
        List<Role> allRoles = userService.findAllRoles(); // Pobierz wszystkie dostępne role

        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoleIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toSet()));

        model.addAttribute("userRoleUpdateDto", dto);
        model.addAttribute("allRoles", allRoles); // Przekaż wszystkie role do widoku
        return "admin/user-edit-roles"; // Widok: templates/admin/user-edit-roles.html
    }

    // Przetwarzanie formularza edycji ról
    @PostMapping("/users/update-roles")
    public String updateUserRoles(@Valid @ModelAttribute("userRoleUpdateDto") UserRoleUpdateDto userRoleUpdateDto,
                                  BindingResult result, // Na razie DTO nie ma walidacji, ale można dodać
                                  RedirectAttributes redirectAttributes,
                                  Model model) { // Model na wypadek błędów bez redirect

        if (result.hasErrors()) {
            // Jeśli DTO miałoby walidacje, tutaj można by je obsłużyć
            // Trzeba by ponownie załadować 'allRoles' do modelu
            model.addAttribute("allRoles", userService.findAllRoles());
            return "admin/user-edit-roles";
        }

        try {
            userService.updateUserRoles(userRoleUpdateDto.getUserId(), userRoleUpdateDto.getRoleIds());
            redirectAttributes.addFlashAttribute("successMessage", "Role użytkownika " + userRoleUpdateDto.getUsername() + " zostały zaktualizowane.");
        } catch (Exception e) {
            // Lepsza obsługa błędów, np. ResourceNotFoundException
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas aktualizacji ról: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}