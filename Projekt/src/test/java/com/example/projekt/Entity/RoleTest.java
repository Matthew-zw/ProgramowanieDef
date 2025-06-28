package com.example.projekt.Entity;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;

class RoleTest {

    @Test
    void equalsAndHashCode_verify() {
        // Stwórz dwie RÓŻNE instancje User
        User u1 = new User("user1", "p1", "User One", "u1@test.com");
        u1.setId(1L);
        User u2 = new User("user2", "p2", "User Two", "u2@test.com");
        u2.setId(2L);

        EqualsVerifier.forClass(Role.class)
                .suppress(Warning.NONFINAL_FIELDS)
                // --- POPRAWKA: Przerwij cykl, dostarczając gotowe obiekty User ---
                .withPrefabValues(User.class, u1, u2)
                // WAŻNE: Zawsze ignoruj pole z `mappedBy` w relacji dwukierunkowej!
                .withIgnoredFields("users")
                .verify();
    }
}