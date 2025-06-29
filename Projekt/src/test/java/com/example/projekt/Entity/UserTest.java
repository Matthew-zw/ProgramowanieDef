package com.example.projekt.Entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private User user1;
    private User user2;
    private Role roleAdmin;
    private Role roleEmployee;

    @BeforeEach
    void setUp() {
        user1 = new User("testuser", "password", "Test User", "test@example.com");
        user1.setId(1L);
        user2 = new User("testuser", "password2", "Test User Two", "test2@example.com");
        user2.setId(2L);

        roleAdmin = new Role("ROLE_ADMIN");
        roleAdmin.setId(10L);

        roleEmployee = new Role("ROLE_EMPLOYEE");
        roleEmployee.setId(11L);
    }

    @Test
    void testGettersAndSetters() {
        user1.setTwoFactorSecret("secret");
        user1.setTwoFactorEnabled(true);
        user1.setEnabled(false);

        assertEquals(1L, user1.getId());
        assertEquals("testuser", user1.getUsername());
        assertEquals("password", user1.getPassword());
        assertEquals("Test User", user1.getFullName());
        assertEquals("test@example.com", user1.getEmail());
        assertEquals("secret", user1.getTwoFactorSecret());
        assertTrue(user1.isTwoFactorEnabled());
        assertFalse(user1.isEnabled());
    }

    @Test
    void testUserDetailsMethods() {
        assertTrue(user1.isAccountNonExpired());
        assertTrue(user1.isAccountNonLocked());
        assertTrue(user1.isCredentialsNonExpired());
    }

    @Test
    void testAddAndRemoveRole() {
        assertTrue(user1.getRoles().isEmpty());

        user1.addRole(roleAdmin);

        assertEquals(1, user1.getRoles().size());
        assertTrue(user1.getRoles().contains(roleAdmin));
        assertTrue(roleAdmin.getUsers().contains(user1));

        user1.removeRole(roleAdmin);

        assertTrue(user1.getRoles().isEmpty());
        assertFalse(roleAdmin.getUsers().contains(user1));
    }

    @Test
    void getAuthorities_returnsCorrectAuthorities() {
        Set<Role> roles = new HashSet<>();
        roles.add(roleAdmin);
        roles.add(roleEmployee);
        user1.setRoles(roles);

        Collection<? extends GrantedAuthority> authorities = user1.getAuthorities();

        assertEquals(2, authorities.size());
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_ADMIN")));
        assertTrue(authorities.contains(new SimpleGrantedAuthority("ROLE_EMPLOYEE")));
    }

    @Test
    void equalsAndHashCode_verify() {
        Role r1 = new Role("ROLE_ONE");
        r1.setId(1L);
        Role r2 = new Role("ROLE_TWO");
        r2.setId(2L);

        Project p1 = new Project();
        p1.setId(101L);
        Project p2 = new Project();
        p2.setId(102L);

        EqualsVerifier.forClass(User.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .withPrefabValues(Role.class, r1, r2)
                .withPrefabValues(Project.class, p1, p2)
                .withIgnoredFields("id", "password", "fullName", "email", "enabled", "twoFactorSecret", "twoFactorEnabled", "roles", "projects", "serialVersionUID")
                .verify();
    }
}