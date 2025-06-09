package com.example.projekt.controller;
import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.dto.UserRoleUpdateDto;
import com.example.projekt.exception.ResourceNotFoundException;
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
@PreAuthorize("hasRole('ROLE_ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;
    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        return "admin/users-list";
    }
    @GetMapping("/users/edit-roles/{userId}")
    public String showEditUserRolesForm(@PathVariable Long userId, Model model) {
        User user = userService.findUserById(userId);
        List<Role> allRoles = userService.findAllRoles();

        UserRoleUpdateDto dto = new UserRoleUpdateDto();
        dto.setUserId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRoleIds(user.getRoles().stream().map(Role::getId).collect(Collectors.toSet()));

        model.addAttribute("userRoleUpdateDto", dto);
        model.addAttribute("allRoles", allRoles);
        return "admin/user-edit-roles";
    }
    @PostMapping("/users/update-roles")
    public String updateUserRoles(@Valid @ModelAttribute("userRoleUpdateDto") UserRoleUpdateDto userRoleUpdateDto,
                                  BindingResult result,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {

        if (result.hasErrors()) {
            model.addAttribute("allRoles", userService.findAllRoles());
            return "admin/user-edit-roles";
        }

        try {
            userService.updateUserRoles(userRoleUpdateDto.getUserId(), userRoleUpdateDto.getRoleIds());
            redirectAttributes.addFlashAttribute("successMessage", "Role użytkownika " + userRoleUpdateDto.getUsername() + " zostały zaktualizowane.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas aktualizacji ról: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
    @PostMapping("/users/delete/{userId}")
    public String deleteUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {
        try {
            User user = userService.findUserById(userId);
            userService.deleteUserById(userId);
            redirectAttributes.addFlashAttribute("successMessage", "Użytkownik '" + user.getUsername() + "' został pomyślnie usunięty.");
        } catch (ResourceNotFoundException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono użytkownika o podanym ID.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas usuwania użytkownika: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}