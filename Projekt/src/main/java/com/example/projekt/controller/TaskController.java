package com.example.projekt.controller;

import com.example.projekt.dto.CreateTaskRequest;
import com.example.projekt.dto.TaskDTO;
import com.example.projekt.dto.UpdateTaskRequest;
import com.example.projekt.Entity.TaskStatus; // For dropdown
import com.example.projekt.service.ProjectService;
import com.example.projekt.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/tasks") // Tasks are always related to a project
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final ProjectService projectService; // To get project name for display

    // GET /projects/{projectId}/tasks - Display tasks for a specific project
    @GetMapping
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public String getTasksByProject(@PathVariable Long projectId, Model model) {
        List<TaskDTO> tasks = taskService.getTasksByProjectId(projectId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("project", projectService.getProjectById(projectId)); // For project info
        model.addAttribute("projectId", projectId);
        return "tasks/list"; // Thymeleaf template: templates/tasks/list.html
    }

    // GET /projects/{projectId}/tasks/new - Display form to create a new task for a project
    @GetMapping("/new")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public String showCreateTaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("createTaskRequest", new CreateTaskRequest());
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", projectService.getProjectById(projectId));
        model.addAttribute("taskStatuses", TaskStatus.values()); // For status dropdown
        return "tasks/form"; // Thymeleaf template: templates/tasks/form.html
    }

    // POST /projects/{projectId}/tasks - Create a new task
    @PostMapping
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public String createTask(@PathVariable Long projectId,
                             @Valid @ModelAttribute("createTaskRequest") CreateTaskRequest request,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projectId", projectId);
            model.addAttribute("project", projectService.getProjectById(projectId));
            model.addAttribute("taskStatuses", TaskStatus.values());
            return "tasks/form";
        }
        taskService.createTask(projectId, request);
        return "redirect:/projects/" + projectId + "/tasks";
    }


    // GET /projects/{projectId}/tasks/edit/{taskId} - Display form to edit a task
    @GetMapping("/edit/{taskId}")
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    public String showUpdateTaskForm(@PathVariable Long projectId, @PathVariable Long taskId, Model model) {
        TaskDTO taskDTO = taskService.getTaskById(taskId);
        UpdateTaskRequest updateRequest = new UpdateTaskRequest();
        updateRequest.setTitle(taskDTO.getTitle());
        updateRequest.setDescription(taskDTO.getDescription());
        updateRequest.setStatus(taskDTO.getStatus());
        updateRequest.setDueDate(taskDTO.getDueDate());

        model.addAttribute("updateTaskRequest", updateRequest);
        model.addAttribute("projectId", projectId);
        model.addAttribute("taskId", taskId);
        model.addAttribute("project", projectService.getProjectById(projectId));
        model.addAttribute("taskStatuses", TaskStatus.values());
        return "tasks/edit-form"; // Thymeleaf template: templates/tasks/edit-form.html
    }

    // POST /projects/{projectId}/tasks/update/{taskId} - Update task
    @PreAuthorize("hasRole('ROLE_EMPLOYEE')")
    @PostMapping("/update/{taskId}")
    public String updateTask(@PathVariable Long projectId, @PathVariable Long taskId,
                             @Valid @ModelAttribute("updateTaskRequest") UpdateTaskRequest request,
                             BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projectId", projectId);
            model.addAttribute("taskId", taskId);
            model.addAttribute("project", projectService.getProjectById(projectId));
            model.addAttribute("taskStatuses", TaskStatus.values());
            return "tasks/edit-form";
        }
        taskService.updateTask(taskId, request);
        return "redirect:/projects/" + projectId + "/tasks";
    }

    // POST /projects/{projectId}/tasks/delete/{taskId} - Delete task
    @PostMapping("/delete/{taskId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String deleteTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return "redirect:/projects/" + projectId + "/tasks";
    }
}