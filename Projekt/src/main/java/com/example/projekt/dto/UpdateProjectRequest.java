package com.example.projekt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateProjectRequest {
    @NotBlank(message = "Project name cannot be blank")
    @Size(max =100, message = "Project name cannot exceed 100 characters")
    private String name;
    @Size(max = 500, message = "Descriptioon cannot exceed 500 characters")
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
}
