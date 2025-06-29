package com.example.projekt.Entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @NotBlank
    @Column(nullable = false)
    private String password;

    @NotBlank
    @Column(length = 100)
    private String fullName;
    @Email
    @Column(unique = true, length = 100)
    private String email;

    private boolean enabled = true;
    @Column(length = 64)
    private String twoFactorSecret;

    private boolean twoFactorEnabled = false;

    @ManyToMany(fetch = FetchType.EAGER, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )

    private Set<Role> roles = new HashSet<>();

    /**
     *
     * @param username
     * @param password
     * @param fullName
     * @param email
     */
    public User(String username, String password, String fullName, String email) {
        this.username = username;
        this.password = password;
        this.fullName = fullName;
        this.email = email;
    }

    /**
     *
     * @param role
     */
    public void addRole(Role role) {
        this.roles.add(role);
        role.getUsers().add(this);
    }

    /**
     *
     * @param role
     */
    public void removeRole(Role role) {
        this.roles.remove(role);
        role.getUsers().remove(this);
    }

    /**
     *
     * @return
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toList());
    }

    /**
     *
     * @return
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     *
     * @return
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     *
     * @return
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return username != null ? username.equals(user.username) : user.username == null;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return username != null ? username.hashCode() : 0;
    }

    @ManyToMany(mappedBy = "assignedUsers", fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Project> projects = new HashSet<>();
}