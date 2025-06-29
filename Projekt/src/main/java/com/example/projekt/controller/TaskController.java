package com.example.projekt.controller;

import com.example.projekt.dto.CreateTaskRequest;
import com.example.projekt.dto.TaskDTO;
import com.example.projekt.dto.UpdateTaskRequest;
import com.example.projekt.Entity.TaskStatus;
import com.example.projekt.service.ProjectService;
import com.example.projekt.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/projects/{projectId}/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;
    private final ProjectService projectService;

    /**
     *
     * @param projectId
     * @param model
     * @return
     */
    @GetMapping
    public String getTasksByProject(@PathVariable Long projectId, Model model) {
        List<TaskDTO> tasks = taskService.getTasksByProjectId(projectId);
        model.addAttribute("tasks", tasks);
        model.addAttribute("project", projectService.getProjectById(projectId));
        model.addAttribute("projectId", projectId);
        return "tasks/list";
    }

    /**
     *
     * @param projectId
     * @param model
     * @return
     */
    @GetMapping("/new")
    public String showCreateTaskForm(@PathVariable Long projectId, Model model) {
        model.addAttribute("createTaskRequest", new CreateTaskRequest());
        model.addAttribute("projectId", projectId);
        model.addAttribute("project", projectService.getProjectById(projectId));
        model.addAttribute("taskStatuses", TaskStatus.values());
        return "tasks/form";
    }

    /**
     *
     * @param projectId
     * @param request
     * @param result
     * @param model
     * @return
     */
    @PostMapping
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

    /**
     *
     * @param projectId
     * @param taskId
     * @param model
     * @return
     */
    @GetMapping("/edit/{taskId}")
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
        return "tasks/edit-form";
    }

    /**
     *
     * @param projectId
     * @param taskId
     * @param request
     * @param result
     * @param model
     * @return
     */
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

    /**
     *
     * @param projectId
     * @param taskId
     * @return
     */
    @PostMapping("/delete/{taskId}")
    public String deleteTask(@PathVariable Long projectId, @PathVariable Long taskId) {
        taskService.deleteTask(taskId);
        return "redirect:/projects/" + projectId + "/tasks";
    }
}