package com.example.projekt.dto;

import com.example.projekt.Entity.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateTaskRequest {
    @NotBlank(message = "Task title cannot be blank")
    @Size(max = 150, message = "Task title cannot exceed 150 characters")
    private String title;
    @Size(max = 1000, message = "Task description cannot exceed 1000 characters")
    private String description;
    @NotNull(message = "Status must be provided")
    private TaskStatus status = TaskStatus.TODO;
    private LocalDateTime dueDate;

}
