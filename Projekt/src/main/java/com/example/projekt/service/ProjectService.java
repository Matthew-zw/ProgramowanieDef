package com.example.projekt.service;


import com.example.projekt.Entity.Project;
import com.example.projekt.dto.CreateProjectRequest;
import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.dto.UpdateProjectRequest;
import com.example.projekt.exception.ResourceNotFoundException;
import com.example.projekt.repository.ProjectRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {
    private final ProjectRepository projectRepository;

    private ProjectDTO maptoProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        return dto;
    }
    public ProjectDTO createProject(CreateProjectRequest request){
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Project savedProject = projectRepository.save(project);
        return maptoProjectDTO(savedProject);
    }
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects(){
        return projectRepository.findAll().stream().map(this::maptoProjectDTO).collect(Collectors.toList());
    }
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project","id",projectId));
        return maptoProjectDTO(project);
    }
    protected Project findProjectEntityyById(Long projectId){
        return projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project","id",projectId));
    }

    public ProjectDTO updateProject(Long projectId, UpdateProjectRequest request){
        Project project = findProjectEntityyById(projectId);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        Project updatedProject = projectRepository.save(project);
        return maptoProjectDTO(updatedProject);
    }

    public void deleteProject(Long projectId){
        Project project = findProjectEntityyById(projectId);
        projectRepository.delete(project);
    }
}

