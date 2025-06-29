package com.example.projekt.Entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void equalsAndHashCode_verify() {
        User u1 = new User("user1", "p1", "User One", "u1@test.com");
        u1.setId(1L);
        User u2 = new User("user2", "p2", "User Two", "u2@test.com");
        u2.setId(2L);

        EqualsVerifier.forClass(Role.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .withPrefabValues(User.class, u1, u2)
                .withIgnoredFields("users")
                .verify();
    }
}