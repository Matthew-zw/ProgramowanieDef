package com.example.projekt.dto;

import lombok.Data;
import java.util.Set;

@Data
public final class AssignUsersToProjectDto {
    private Long projectId;
    private String projectName;
    private Set<Long> userIds;
}