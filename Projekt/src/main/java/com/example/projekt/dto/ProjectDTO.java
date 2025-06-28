package com.example.projekt.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public final class ProjectDTO {
    private Long id;
    private String name;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
