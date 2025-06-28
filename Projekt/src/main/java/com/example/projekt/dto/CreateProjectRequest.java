package com.example.projekt.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public final class CreateProjectRequest {
   @NotBlank(message = "Project Name cannot be blank")
   @Size(max = 100, message = "Project name cannot exceed 100 characters")
   private String name;
   @Size(max = 500, message = "Description cannot exceed 500 characters")
   private String description;

   private LocalDate startDate;
   private LocalDate endDate;
}
