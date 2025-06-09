package com.example.projekt.config;

import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.repository.RoleRepository;
import com.example.projekt.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;


@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        Role adminRole = createRoleIfNotFound("ROLE_ADMIN");
        Role managerRole = createRoleIfNotFound("ROLE_PROJECT_MANAGER");
        Role employeeRole = createRoleIfNotFound("ROLE_EMPLOYEE");
        createUserIfNotFound("admin", "admin123", "Administrator", "admin@example.com", Set.of("ROLE_ADMIN"));
        createUserIfNotFound("manager", "manager123", "Manager", "manager@example.com", Set.of("ROLE_PROJECT_MANAGER", "ROLE_EMPLOYEE"));
        createUserIfNotFound("user", "user123", "User", "user@example.com", Set.of("ROLE_EMPLOYEE"));
    }

    private Role createRoleIfNotFound(String name) {
        return roleRepository.findByName(name)
                .orElseGet(() -> roleRepository.save(new Role(name)));
    }
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
                managedRoles.add(role);
            }
            user.setRoles(managedRoles);

            userRepository.save(user);
        }
    }
}