package com.example.projekt.controller;

import com.example.projekt.dto.CreateProjectRequest;
import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.dto.UpdateProjectRequest;
import com.example.projekt.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller; // Changed from @RestController
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

// Changed from @RestController to @Controller for Thymeleaf
@Controller
@RequestMapping("/projects") // Base path for all project-related views
@RequiredArgsConstructor
// @CrossOrigin // Not strictly needed for server-side rendering from the same origin
public class ProjectController {
    private final ProjectService projectService;

    // GET /projects - Display all projects
    @GetMapping
    public String getAllProjects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        return "projects/list"; // Thymeleaf template: templates/projects/list.html
    }

    // GET /projects/new - Display form to create a new project
    @GetMapping("/new")
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String showCreateProjectForm(Model model) {
        model.addAttribute("createProjectRequest", new CreateProjectRequest());
        return "projects/form"; // Thymeleaf template: templates/projects/form.html
    }

    // POST /projects - Create a new Project
    @PostMapping
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String createProject(@Valid @ModelAttribute("createProjectRequest") CreateProjectRequest request,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "projects/form"; // Return to form if validation errors
        }
        projectService.createProject(request);
        return "redirect:/projects"; // Redirect to project list
    }
    // GET /projects/edit/{projectId} - Display form to edit a project
    @GetMapping("/edit/{projectId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String showUpdateProjectForm(@PathVariable Long projectId, Model model) {
        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        // Map ProjectDTO to UpdateProjectRequest or use ProjectDTO directly if form fields match
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setName(projectDTO.getName());
        updateRequest.setDescription(projectDTO.getDescription());
        updateRequest.setStartDate(projectDTO.getStartDate());
        updateRequest.setEndDate(projectDTO.getEndDate());

        model.addAttribute("updateProjectRequest", updateRequest);
        model.addAttribute("projectId", projectId);
        return "projects/edit-form"; // Thymeleaf template: templates/projects/edit-form.html
    }

    // POST /projects/update/{projectId} - Update project
    @PostMapping("/update/{projectId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String updateProject(@PathVariable Long projectId,
                                @Valid @ModelAttribute("updateProjectRequest") UpdateProjectRequest request,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projectId", projectId); // Keep projectId for the form action
            return "projects/edit-form";
        }
        projectService.updateProject(projectId, request);
        return "redirect:/projects";
    }

    // POST /projects/delete/{projectId} - Delete project (using POST for simplicity with HTML forms)
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    @PostMapping("/delete/{projectId}")
    public String deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return "redirect:/projects";
    }

    // GET /projects/{projectId} - View project details and its tasks
    // This will be handled by TaskController or a dedicated project details page
}