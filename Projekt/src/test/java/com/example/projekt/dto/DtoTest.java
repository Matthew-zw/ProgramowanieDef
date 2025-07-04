package com.example.projekt.dto;

import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.Test;


class DtoTest {

    @Test
    void testAssignUsersToProjectDto() {
        EqualsVerifier.forClass(AssignUsersToProjectDto.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testCreateProjectRequest() {
        EqualsVerifier.forClass(CreateProjectRequest.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testCreateTaskRequest() {
        EqualsVerifier.forClass(CreateTaskRequest.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testProjectDTO() {
        EqualsVerifier.forClass(ProjectDTO.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testTaskDTO() {
        EqualsVerifier.forClass(TaskDTO.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testTwoFactorSetupDto() {
        EqualsVerifier.forClass(TwoFactorSetupDto.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testUpdateProjectRequest() {
        EqualsVerifier.forClass(UpdateProjectRequest.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testUpdateTaskRequest() {
        EqualsVerifier.forClass(UpdateTaskRequest.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testUserDTO() {
        EqualsVerifier.forClass(UserDTO.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testUserRegistrationDto() {
        EqualsVerifier.forClass(UserRegistrationDto.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testUserRoleUpdateDto() {
        EqualsVerifier.forClass(UserRoleUpdateDto.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }

    @Test
    void testVerifyTotpCodeDto() {
        EqualsVerifier.forClass(VerifyTotpCodeDto.class)
                .suppress(Warning.NONFINAL_FIELDS)
                .verify();
    }
}