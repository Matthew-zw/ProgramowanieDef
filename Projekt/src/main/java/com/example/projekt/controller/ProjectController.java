package com.example.projekt.controller;


import com.example.projekt.dto.CreateProjectRequest;
import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.dto.UpdateProjectRequest;
import com.example.projekt.repository.ProjectRepository;
import com.example.projekt.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("api/projects")
@RequiredArgsConstructor
@CrossOrigin
public class ProjectController {
    private final ProjectService projectService;
    // POST /api/projects Create a new Project
    @PostMapping
    public ResponseEntity<ProjectDTO> createProject(@Valid @RequestBody CreateProjectRequest request){
        ProjectDTO createdProject = projectService.createProject(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(createdProject.getId()).toUri();
        return ResponseEntity.created(location).body(createdProject);
    }
    // GET /api/projects Get all projects
    @GetMapping
    public ResponseEntity<List<ProjectDTO>> getAllProjects(){
        List<ProjectDTO> projects = projectService.getAllProjects();
        return ResponseEntity.ok(projects);
    }
    // GET /api/projects/{id}
    @GetMapping("/{projectid}")
    public ResponseEntity<ProjectDTO> getProjectById(@PathVariable Long projectid){
        ProjectDTO project = projectService.getProjectById(projectid);
        return ResponseEntity.ok(project);
    }
    //PUT /api/projects/{id} Update project
    @PutMapping("/{projectId}")
    public ResponseEntity<ProjectDTO> updateProject(@PathVariable Long projectId, @Valid @RequestBody UpdateProjectRequest request){
        ProjectDTO updatedProject = projectService.updateProject(projectId, request);
        return ResponseEntity.ok(updatedProject);
    }
    // DELETE /api/projects/{id} delete project
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long projectId){
        projectService.deleteProject(projectId);
        return ResponseEntity.noContent().build();
    }
}
