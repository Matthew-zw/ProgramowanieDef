package com.example.projekt.Entity;



import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    /**
     *
     * @param name
     */
    public Role(String name) {
        this.name = name;
    }

    /**
     *
     * @return
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Role role)) return false;
        return name != null ? name.equals(role.name) : role.name == null;
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }
}