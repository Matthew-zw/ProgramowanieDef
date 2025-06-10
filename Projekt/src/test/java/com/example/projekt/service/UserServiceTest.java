package com.example.projekt.service;

import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.dto.TwoFactorSetupDto;
import com.example.projekt.dto.UserRegistrationDto;
import com.example.projekt.exception.ResourceNotFoundException;
import com.example.projekt.exception.UserAlreadyExistAuthenticationException;
import com.example.projekt.repository.RoleRepository;
import com.example.projekt.repository.UserRepository;
import com.example.projekt.util.TotpUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        employeeRole = new Role("ROLE_EMPLOYEE");
        employeeRole.setId(1L);
        adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(2L);

        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setFullName("Test User");
        testUser.setEmail("test@example.com");
        testUser.setEnabled(true);
        testUser.setRoles(new HashSet<>(Collections.singletonList(employeeRole)));


        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);

        lenient().when(authentication.getName()).thenReturn("admin");
    }

    @Test
    void registerNewUser_success() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("newuser");
        dto.setPassword("password123");
        dto.setConfirmPassword("password123");
        dto.setFullName("New User");
        dto.setEmail("newuser@example.com");

        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("encodedNewPassword");
        when(roleRepository.findByName("ROLE_EMPLOYEE")).thenReturn(Optional.of(employeeRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User registeredUser = userService.registerNewUser(dto);

        assertNotNull(registeredUser);
        assertEquals(testUser.getUsername(), registeredUser.getUsername());
        assertTrue(registeredUser.getRoles().contains(employeeRole));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void registerNewUser_usernameExists() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("existinguser");
        dto.setEmail("newuser@example.com");

        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistAuthenticationException.class, () -> userService.registerNewUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUser_emailExists() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setUsername("newuser");
        dto.setEmail("existing@example.com");

        when(userRepository.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(userRepository.findByEmail(dto.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistAuthenticationException.class, () -> userService.registerNewUser(dto));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void usernameExists_true() {
        when(userRepository.findByUsername("existinguser")).thenReturn(Optional.of(new User()));
        assertTrue(userService.usernameExists("existinguser"));
    }

    @Test
    void usernameExists_false() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());
        assertFalse(userService.usernameExists("nonexistent"));
    }

    @Test
    void emailExists_true() {
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(new User()));
        assertTrue(userService.emailExists("existing@example.com"));
    }

    @Test
    void emailExists_false() {
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());
        assertFalse(userService.emailExists("nonexistent@example.com"));
    }

    @Test
    void findAllUsers_returnsListOfUsers() {
        List<User> users = Arrays.asList(testUser, new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> foundUsers = userService.findAllUsers();
        assertNotNull(foundUsers);
        assertEquals(2, foundUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void findUserById_found() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        User foundUser = userService.findUserById(1L);
        assertNotNull(foundUser);
        assertEquals(testUser.getUsername(), foundUser.getUsername());
    }

    @Test
    void findUserById_notFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findUserById(99L));
    }

    @Test
    void findAllRoles_returnsListOfRoles() {
        List<Role> roles = Arrays.asList(employeeRole, adminRole);
        when(roleRepository.findAll()).thenReturn(roles);

        List<Role> foundRoles = userService.findAllRoles();
        assertNotNull(foundRoles);
        assertEquals(2, foundRoles.size());
        verify(roleRepository, times(1)).findAll();
    }

    @Test
    void updateUserRoles_success() {
        Set<Long> newRoleIds = new HashSet<>(Arrays.asList(adminRole.getId(), employeeRole.getId()));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(adminRole.getId())).thenReturn(Optional.of(adminRole));
        when(roleRepository.findById(employeeRole.getId())).thenReturn(Optional.of(employeeRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User updatedUser = userService.updateUserRoles(testUser.getId(), newRoleIds);

        assertNotNull(updatedUser);
        assertEquals(2, updatedUser.getRoles().size());
        assertTrue(updatedUser.getRoles().contains(adminRole));
        assertTrue(updatedUser.getRoles().contains(employeeRole));
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void updateUserRoles_userNotFound() {
        Set<Long> newRoleIds = new HashSet<>(Collections.singletonList(adminRole.getId()));
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRoles(99L, newRoleIds));
    }

    @Test
    void updateUserRoles_roleNotFound() {
        Set<Long> newRoleIds = new HashSet<>(Collections.singletonList(99L));
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(testUser));
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.updateUserRoles(testUser.getId(), newRoleIds));
    }

    @Test
    void setupTwoFactorAuthentication_disabled() {
        testUser.setTwoFactorEnabled(false);
        testUser.setTwoFactorSecret(null);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        try (MockedStatic<TotpUtil> mockedTotpUtil = Mockito.mockStatic(TotpUtil.class)) {
            mockedTotpUtil.when(TotpUtil::generateSecret).thenReturn("GENERATED_SECRET");
            mockedTotpUtil.when(() -> TotpUtil.generateTotpUri(anyString(), anyString(), anyString())).thenReturn("TOTP_URI");
            mockedTotpUtil.when(() -> TotpUtil.generateQrCode(anyString())).thenReturn("QR_CODE_BASE64");

            TwoFactorSetupDto dto = userService.setupTwoFactorAuthentication(testUser.getUsername());

            assertFalse(dto.isEnabled());
            assertEquals("GENERATED_SECRET", dto.getSecret());
            assertEquals("GENERATED_SECRET", dto.getManualEntryKey());
            assertEquals("QR_CODE_BASE64", dto.getQrCodeUri());
            verify(userRepository, times(1)).save(testUser);
        }
    }

    @Test
    void setupTwoFactorAuthentication_enabled() {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorSecret("EXISTING_SECRET");
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        TwoFactorSetupDto dto = userService.setupTwoFactorAuthentication(testUser.getUsername());

        assertTrue(dto.isEnabled());
        assertNull(dto.getSecret());
        assertNull(dto.getManualEntryKey());
        assertNull(dto.getQrCodeUri());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void enableTwoFactorAuthentication_success() {
        testUser.setTwoFactorEnabled(false);
        testUser.setTwoFactorSecret("SOME_SECRET");
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        try (MockedStatic<TotpUtil> mockedTotpUtil = Mockito.mockStatic(TotpUtil.class)) {
            mockedTotpUtil.when(() -> TotpUtil.verifyCode("SOME_SECRET", "123456")).thenReturn(true);

            boolean result = userService.enableTwoFactorAuthentication(testUser.getUsername(), "123456");

            assertTrue(result);
            assertTrue(testUser.isTwoFactorEnabled());
            verify(userRepository, times(1)).save(testUser);
        }
    }

    @Test
    void enableTwoFactorAuthentication_invalidCode() {
        testUser.setTwoFactorEnabled(false);
        testUser.setTwoFactorSecret("SOME_SECRET");
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        try (MockedStatic<TotpUtil> mockedTotpUtil = Mockito.mockStatic(TotpUtil.class)) {
            mockedTotpUtil.when(() -> TotpUtil.verifyCode("SOME_SECRET", "654321")).thenReturn(false);

            boolean result = userService.enableTwoFactorAuthentication(testUser.getUsername(), "654321");

            assertFalse(result);
            assertFalse(testUser.isTwoFactorEnabled());
            verify(userRepository, never()).save(testUser);
        }
    }

    @Test
    void enableTwoFactorAuthentication_secretNotGenerated() {
        testUser.setTwoFactorEnabled(false);
        testUser.setTwoFactorSecret(null);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        assertThrows(IllegalStateException.class, () -> userService.enableTwoFactorAuthentication(testUser.getUsername(), "123456"));
        verify(userRepository, never()).save(testUser);
    }

    @Test
    void disableTwoFactorAuthentication_success() {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorSecret("SOME_SECRET");
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        userService.disableTwoFactorAuthentication(testUser.getUsername());

        assertFalse(testUser.isTwoFactorEnabled());
        assertNull(testUser.getTwoFactorSecret());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void isTwoFactorEnabledForUser_true() {
        testUser.setTwoFactorEnabled(true);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        assertTrue(userService.isTwoFactorEnabledForUser(testUser.getUsername()));
    }

    @Test
    void isTwoFactorEnabledForUser_false() {
        testUser.setTwoFactorEnabled(false);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));
        assertFalse(userService.isTwoFactorEnabledForUser(testUser.getUsername()));
    }

    @Test
    void isTwoFactorEnabledForUser_userNotFound_shouldReturnFalse() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

       boolean result = userService.isTwoFactorEnabledForUser("nonexistent");

        assertFalse(result, "Should return false if user is not found");
    }

    @Test
    void verifyTotpCodeForUser_success() {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorSecret("SOME_SECRET");
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        try (MockedStatic<TotpUtil> mockedTotpUtil = Mockito.mockStatic(TotpUtil.class)) {
            mockedTotpUtil.when(() -> TotpUtil.verifyCode("SOME_SECRET", "123456")).thenReturn(true);

            assertTrue(userService.verifyTotpCodeForUser(testUser.getUsername(), "123456"));
        }
    }

    @Test
    void verifyTotpCodeForUser_invalidCode() {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorSecret("SOME_SECRET");
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        try (MockedStatic<TotpUtil> mockedTotpUtil = Mockito.mockStatic(TotpUtil.class)) {
            mockedTotpUtil.when(() -> TotpUtil.verifyCode("SOME_SECRET", "654321")).thenReturn(false);

            assertFalse(userService.verifyTotpCodeForUser(testUser.getUsername(), "654321"));
        }
    }

    @Test
    void verifyTotpCodeForUser_2faNotEnabled() {
        testUser.setTwoFactorEnabled(false);
        testUser.setTwoFactorSecret("SOME_SECRET");
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        assertFalse(userService.verifyTotpCodeForUser(testUser.getUsername(), "123456"));
    }

    @Test
    void verifyTotpCodeForUser_noSecret() {
        testUser.setTwoFactorEnabled(true);
        testUser.setTwoFactorSecret(null);
        when(userRepository.findByUsername(testUser.getUsername())).thenReturn(Optional.of(testUser));

        assertFalse(userService.verifyTotpCodeForUser(testUser.getUsername(), "123456"));
    }

    @Test
    void deleteUserById_success() {
        User userToDelete = new User();
        userToDelete.setId(2L);
        userToDelete.setUsername("userToDelete");

        when(userRepository.findById(userToDelete.getId())).thenReturn(Optional.of(userToDelete));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        when(authentication.getName()).thenReturn("admin");

        userService.deleteUserById(userToDelete.getId());

        verify(userRepository, times(1)).delete(userToDelete);
    }

    @Test
    void deleteUserById_userNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserById(99L));
        verify(userRepository, never()).delete(any(User.class));
    }

    @Test
    void deleteUserById_cannotDeleteSelf() {
        User userToDelete = testUser;
        when(userRepository.findById(testUser.getId())).thenReturn(Optional.of(userToDelete));

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        when(authentication.getName()).thenReturn(userToDelete.getUsername());

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> userService.deleteUserById(testUser.getId()));
        assertEquals("Nie możesz usunąć własnego konta.", thrown.getMessage());
        verify(userRepository, never()).delete(any(User.class));
    }
}