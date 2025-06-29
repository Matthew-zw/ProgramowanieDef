package com.example.projekt.service;

import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.dto.UserRegistrationDto;
import com.example.projekt.exception.ResourceNotFoundException;
import com.example.projekt.exception.UserAlreadyExistAuthenticationException;
import com.example.projekt.repository.RoleRepository;
import com.example.projekt.repository.UserRepository;
import com.example.projekt.util.TotpUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import com.example.projekt.dto.TwoFactorSetupDto;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     *
     * @param registrationDto
     * @return
     * @throws UserAlreadyExistAuthenticationException
     */
    @Transactional
    public User registerNewUser(UserRegistrationDto registrationDto) throws UserAlreadyExistAuthenticationException {
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

    /**
     *
     * @param username
     * @return
     */
    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    /**
     *
     * @param email
     * @return
     */
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }

    /**
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    /**
     *
     * @param userId
     * @return
     */
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
    }

    /**
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<Role> findAllRoles() {
        return roleRepository.findAll();
    }

    /**
     *
     * @param userId
     * @param roleIds
     * @return
     */
    @Transactional
    public User updateUserRoles(Long userId, Set<Long> roleIds) {
        User user = findUserById(userId);
        Set<Role> newRoles = roleIds.stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new ResourceNotFoundException("Role", "id", roleId)))
                .collect(Collectors.toSet());

        user.setRoles(newRoles);
        return userRepository.save(user);
    }

    /**
     *
     * @param username
     * @return
     */
    @Transactional
    public TwoFactorSetupDto setupTwoFactorAuthentication(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));

        TwoFactorSetupDto dto = new TwoFactorSetupDto();
        if (user.isTwoFactorEnabled()) {
            dto.setEnabled(true);
        } else {
            String secret = TotpUtil.generateSecret();
            user.setTwoFactorSecret(secret);
            userRepository.save(user);
            String issuer = "TwojaAplikacja";
            String totpUri = TotpUtil.generateTotpUri(issuer, user.getUsername(), secret);
            dto.setEnabled(false);
            dto.setSecret(secret);
            dto.setManualEntryKey(secret);
            dto.setQrCodeUri(TotpUtil.generateQrCode(totpUri));
        }
        return dto;
    }

    /**
     *
     * @param username
     * @param code
     * @return
     */
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

    /**
     *
     * @param username
     */
    @Transactional
    public void disableTwoFactorAuthentication(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
    }

    /**
     *
     * @param username
     * @return
     */
    public boolean isTwoFactorEnabledForUser(String username) {
        return userRepository.findByUsername(username)
                .map(User::isTwoFactorEnabled)
                .orElse(false);
    }

    /**
     *
     * @param username
     * @param code
     * @return
     */
    public boolean verifyTotpCodeForUser(String username, String code) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
        if (user.getTwoFactorSecret() == null || !user.isTwoFactorEnabled()) {
            return false;
        }
        return TotpUtil.verifyCode(user.getTwoFactorSecret(), code);
    }

    /**
     *
     * @param userId
     */
    @Transactional
    public void deleteUserById(Long userId) {
        User userToDelete = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getName().equals(userToDelete.getUsername())) {
            throw new IllegalArgumentException("Nie możesz usunąć własnego konta.");
        }
        

        userRepository.delete(userToDelete);
    }
}
