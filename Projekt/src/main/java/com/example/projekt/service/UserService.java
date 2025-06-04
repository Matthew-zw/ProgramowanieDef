package com.example.projekt.service;

import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.dto.UserRegistrationDto;
import com.example.projekt.dto.UserRoleUpdateDto; // Import
import com.example.projekt.exception.ResourceNotFoundException; // Import, jeśli go masz
import com.example.projekt.exception.UserAlreadyExistAuthenticationException;
import com.example.projekt.repository.RoleRepository;
import com.example.projekt.repository.UserRepository;
import com.example.projekt.util.TotpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashSet;
import java.util.List; // Import
import java.util.Set; // Import
import java.util.stream.Collectors;
import com.example.projekt.dto.TwoFactorSetupDto;
import com.example.projekt.util.TotpUtil;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) throws UserAlreadyExistAuthenticationException {
        // ... (istniejący kod rejestracji)
        if (userRepository.findByUsername(registrationDto.getUsername()).isPresent()) {
            throw new UserAlreadyExistAuthenticationException("Użytkownik o nazwie '" + registrationDto.getUsername() + "' już istnieje.");
        }
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new UserAlreadyExistAuthenticationException("Użytkownik z adresem email '" + registrationDto.getEmail() + "' już istnieje.");
        }

        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setEmail(registrationDto.getEmail());
        user.setEnabled(true);

        Role userRole = roleRepository.findByName("ROLE_EMPLOYEE")
                .orElseThrow(() -> new RuntimeException("Błąd: Rola ROLE_EMPLOYEE nie została znaleziona."));
        user.setRoles(new HashSet<>(Collections.singletonList(userRole)));

        return userRepository.save(user);
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    // --- NOWE METODY DLA ADMINA ---

    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }


    @Transactional
    public User updateUserRoles(Long userId, Set<Long> roleIds) {
        User user = findUserById(userId);

        // Unikaj modyfikowania ról superadmina, jeśli taki istnieje i nie chcesz na to pozwolić
        // if (user.getUsername().equals("admin") && !currentUserIsSuperAdmin()) {
        // throw new AccessDeniedException("Nie można modyfikować ról głównego administratora.");
        // }

        Set<Role> newRoles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        return userRepository.save(user);
    }
    @Transactional
    public TwoFactorSetupDto setupTwoFactorAuthentication(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        TwoFactorSetupDto dto = new TwoFactorSetupDto();
        if (user.isTwoFactorEnabled()) {
            // Jeśli 2FA jest już włączone, możemy pozwolić na wygenerowanie nowego sekretu
            // lub po prostu wyświetlić, że jest włączone. Dla uproszczenia, na razie tylko informacja.
            dto.setEnabled(true);
            // Nie pokazujemy sekretu ani QR, jeśli już jest ustawione i aktywne.
            // Użytkownik musiałby najpierw wyłączyć 2FA, aby zobaczyć nowy QR.
        } else {
            String secret = TotpUtil.generateSecret();
            user.setTwoFactorSecret(secret); // Na razie tylko ustawiamy, aktywacja po weryfikacji pierwszego kodu
            userRepository.save(user); // Zapisz sekret w bazie

            String issuer = "TwojaAplikacja"; // Zmień na nazwę swojej aplikacji
            String totpUri = TotpUtil.generateTotpUri(issuer, user.getUsername(), secret);

            dto.setEnabled(false);
            dto.setSecret(secret); // Przekazujemy, aby użytkownik mógł zapisać (opcjonalne, jeśli QR wystarczy)
            dto.setManualEntryKey(secret);
            dto.setQrCodeUri(TotpUtil.generateQrCode(totpUri));
        }
        return dto;
    }

    @Transactional
    public boolean enableTwoFactorAuthentication(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        if (user.getTwoFactorSecret() == null) {
            throw new IllegalStateException("Sekret 2FA nie został jeszcze wygenerowany dla tego użytkownika.");
        }

        if (TotpUtil.verifyCode(user.getTwoFactorSecret(), code)) {
            user.setTwoFactorEnabled(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    @Transactional
    public void disableTwoFactorAuthentication(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null); // Opcjonalnie: usuń sekret po wyłączeniu
        userRepository.save(user);
    }

    public boolean isTwoFactorEnabledForUser(String username) {
        return userRepository.findByUsername(username)
                .map(User::isTwoFactorEnabled)
                .orElse(false);
    }

    public boolean verifyTotpCodeForUser(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        if (user.getTwoFactorSecret() == null || !user.isTwoFactorEnabled()) {
            return false; // Lub rzuć wyjątek
        }
        return TotpUtil.verifyCode(user.getTwoFactorSecret(), code);
    }
}
