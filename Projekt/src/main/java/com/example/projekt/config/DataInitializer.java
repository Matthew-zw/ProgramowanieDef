package com.example.projekt.config;

import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.repository.RoleRepository;
import com.example.projekt.repository.UserRepository;
import jakarta.transaction.Transactional; // Ważne dla operacji w ramach jednej transakcji
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional // Dodajemy @Transactional, aby cała inicjalizacja była w jednej transakcji
    public void run(String... args) throws Exception {
        // Tworzenie ról, jeśli nie istnieją
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");
        Role managerRole = createRoleIfNotFound("ROLE_PROJECT_MANAGER");
        Role employeeRole = createRoleIfNotFound("ROLE_EMPLOYEE");

        // Tworzenie użytkownika admin, jeśli nie istnieje
        createUserIfNotFound("admin", "admin123", "Admin Istrator", "admin@example.com", Set.of("ROLE_ADMIN", "ROLE_PROJECT_MANAGER", "ROLE_EMPLOYEE"));
        // Tworzenie użytkownika kierownika, jeśli nie istnieje
        createUserIfNotFound("manager", "manager123", "Manager Kierownik", "manager@example.com", Set.of("ROLE_PROJECT_MANAGER", "ROLE_EMPLOYEE"));
        // Tworzenie użytkownika pracownika, jeśli nie istnieje
        createUserIfNotFound("employee", "employee123", "Employee Pracownik", "employee@example.com", Set.of("ROLE_EMPLOYEE"));
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }

    // Zmodyfikowana metoda createUserIfNotFound
    private void createUserIfNotFound(String username, String password, String fullName, String email, Set<String> roleNames) {
        if (userRepository.findByUsername(username).isEmpty()) {
            User user = new User();
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            user.setEmail(email);
            user.setEnabled(true);

            Set<Role> managedRoles = new HashSet<>();
            for (String roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
                managedRoles.add(role); // Dodajemy role, które są zarządzane (pobrane w tej samej transakcji)
            }
            user.setRoles(managedRoles);

            userRepository.save(user);
        }
    }
}