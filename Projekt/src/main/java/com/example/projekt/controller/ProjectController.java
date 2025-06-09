package com.example.projekt.controller;

import com.example.projekt.dto.CreateProjectRequest;
import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.dto.UpdateProjectRequest;
import com.example.projekt.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor

public class ProjectController {
    private final ProjectService projectService;

    @GetMapping
    public String getAllProjects(Model model) {
        model.addAttribute("projects", projectService.getAllProjects());
        return "projects/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String showCreateProjectForm(Model model) {
        model.addAttribute("createProjectRequest", new CreateProjectRequest());
        return "projects/form";
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String createProject(@Valid @ModelAttribute("createProjectRequest") CreateProjectRequest request,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "projects/form";
        }
        projectService.createProject(request);
        return "redirect:/projects";
    }
    @GetMapping("/edit/{projectId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String showUpdateProjectForm(@PathVariable Long projectId, Model model) {
        ProjectDTO projectDTO = projectService.getProjectById(projectId);
        UpdateProjectRequest updateRequest = new UpdateProjectRequest();
        updateRequest.setName(projectDTO.getName());
        updateRequest.setDescription(projectDTO.getDescription());
        updateRequest.setStartDate(projectDTO.getStartDate());
        updateRequest.setEndDate(projectDTO.getEndDate());

        model.addAttribute("updateProjectRequest", updateRequest);
        model.addAttribute("projectId", projectId);
        return "projects/edit-form";
    }
    @PostMapping("/update/{projectId}")
    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    public String updateProject(@PathVariable Long projectId,
                                @Valid @ModelAttribute("updateProjectRequest") UpdateProjectRequest request,
                                BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("projectId", projectId);
            return "projects/edit-form";
        }
        projectService.updateProject(projectId, request);
        return "redirect:/projects";
    }

    @PreAuthorize("hasRole('ROLE_PROJECT_MANAGER')")
    @PostMapping("/delete/{projectId}")
    public String deleteProject(@PathVariable Long projectId) {
        projectService.deleteProject(projectId);
        return "redirect:/projects";
    }

}