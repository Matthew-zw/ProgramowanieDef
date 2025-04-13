package com.example.projekt.controller;


import com.example.projekt.dto.CreateTaskRequest;
import com.example.projekt.dto.TaskDTO;
import com.example.projekt.dto.UpdateTaskRequest;
import com.example.projekt.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin
public class TaskController {
    private final TaskService taskService;
    // POST /api/projects/{projectId}/tasks Stworz zadanie dla konkretnego projektu
    @PostMapping("/projects/{projectId}/tasks")
    public ResponseEntity<TaskDTO> createTask(@PathVariable Long projectId, @Valid @RequestBody CreateTaskRequest request) {
        TaskDTO createdTask = taskService.createTask(projectId, request);
        URI location = ServletUriComponentsBuilder.fromCurrentContextPath().path("/api/tasks/{id}").buildAndExpand(createdTask.getId()).toUri();
        return ResponseEntity.created(location).body(createdTask);
    }
    //GET /api/projects/{projectId}/tasks wszystkie taski z konkretnego projektu
    @GetMapping("/projects/{projectId}/tasks")
    public ResponseEntity<List<TaskDTO>> getTasksByProject(@PathVariable Long projectId){
        List<TaskDTO> tasks = taskService.getTasksByProjectId(projectId);
        return ResponseEntity.ok(tasks);
    }

    //GET /api/tasks/{taskID} konkretne zadanie w projekcie
    @GetMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long taskId){
        TaskDTO task = taskService.getTaskById(taskId);
        return ResponseEntity.ok(task);
    }
    //PUT /api/tasks/{taskId} aktualizacja zadania
    @PutMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long taskId, @Valid @RequestBody UpdateTaskRequest request) {
        TaskDTO task = taskService.updateTask(taskId, request);
        return ResponseEntity.ok(task);
    }
    //DELETE /api/tasks/{taskId} usun zadanie
    @DeleteMapping("/tasks/{taskId}")
    public ResponseEntity<TaskDTO> deleteTask(@PathVariable Long taskId){
        taskService.deleteTask(taskId);
        return ResponseEntity.noContent().build();
    }
}
