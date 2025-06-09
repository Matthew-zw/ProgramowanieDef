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
import com.example.projekt.dto.AssignUsersToProjectDto;
import com.example.projekt.dto.UserDTO;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor

public class ProjectController {
    private final ProjectService projectService;


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
    @GetMapping("/assign-users/{projectId}")
    @PreAuthorize("hasAuthority('ROLE_PROJECT_MANAGER')")
    public String showAssignUsersForm(@PathVariable Long projectId, Model model) {
        ProjectDTO project = projectService.getProjectById(projectId);
        Set<UserDTO> assignedUsers = projectService.getAssignedUsersForProject(projectId);
        List<UserDTO> allUsers = projectService.getAllUsersAvailableForAssignment();

        AssignUsersToProjectDto dto = new AssignUsersToProjectDto();
        dto.setProjectId(project.getId());
        dto.setProjectName(project.getName());
        dto.setUserIds(assignedUsers.stream().map(UserDTO::getId).collect(Collectors.toSet()));

        model.addAttribute("assignUsersDto", dto);
        model.addAttribute("allAvailableUsers", allUsers);
        return "projects/assign-users-form";
    }


    @PostMapping("/assign-users")
    @PreAuthorize("hasAuthority('ROLE_PROJECT_MANAGER')")
    public String processAssignUsers(@ModelAttribute("assignUsersDto") AssignUsersToProjectDto dto,
                                     RedirectAttributes redirectAttributes) {
        try {
            projectService.assignUsersToProject(dto.getProjectId(), dto.getUserIds());
            redirectAttributes.addFlashAttribute("successMessage", "Użytkownicy zostali pomyślnie przypisani do projektu '" + dto.getProjectName() + "'.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Wystąpił błąd podczas przypisywania użytkowników: " + e.getMessage());
        }
        return "redirect:/projects";
    }
    @GetMapping
    public String listProjects(Model model) {
        model.addAttribute("projects", projectService.getProjectsForCurrentUser());
        return "projects/list";
    }

}