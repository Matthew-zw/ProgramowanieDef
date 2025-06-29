package com.example.projekt.service;


import com.example.projekt.Entity.Project;
import com.example.projekt.Entity.Role;
import com.example.projekt.dto.CreateProjectRequest;
import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.dto.UpdateProjectRequest;
import com.example.projekt.dto.UserDTO;
import com.example.projekt.exception.ResourceNotFoundException;
import com.example.projekt.repository.ProjectRepository;
import com.example.projekt.Entity.User;
import com.example.projekt.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.projekt.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

@Service
@RequiredArgsConstructor
@Transactional
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     *
     * @param project
     * @return
     */
    private ProjectDTO maptoProjectDTO(Project project) {
        ProjectDTO dto = new ProjectDTO();
        dto.setId(project.getId());
        dto.setName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setStartDate(project.getStartDate());
        dto.setEndDate(project.getEndDate());
        return dto;
    }

    /**
     *
     * @param user
     * @return
     */
    private UserDTO mapToUserDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullName(user.getFullName());
        return dto;
    }

    /**
     *
     * @param request
     * @return
     */
    public ProjectDTO createProject(CreateProjectRequest request){
        Project project = new Project();
        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            String username;
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails) {
                username = ((UserDetails) principal).getUsername();
            } else {
                username = principal.toString();
            }
            User projectCreator = userRepository.findByUsername(username)
                    .orElseThrow(() -> new ResourceNotFoundException("User", "username", username + " (creator)"));

            boolean isManager = projectCreator.getAuthorities().stream()
                    .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_PROJECT_MANAGER"));

            if (isManager) {
                project.assignUser(projectCreator);
            }
        }
        Project savedProject = projectRepository.save(project);
        return maptoProjectDTO(savedProject);
    }

    /**
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getAllProjects(){
        return projectRepository.findAll().stream().map(this::maptoProjectDTO).collect(Collectors.toList());
    }

    /**
     *
     * @param projectId
     * @return
     */
    @Transactional(readOnly = true)
    public ProjectDTO getProjectById(Long projectId){
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project","id",projectId));
        return maptoProjectDTO(project);
    }

    /**
     *
     * @param projectId
     * @return
     */
    protected Project findProjectEntityyById(Long projectId){
        return projectRepository.findById(projectId).orElseThrow(() -> new ResourceNotFoundException("Project","id",projectId));
    }

    /**
     *
     * @param projectId
     * @param request
     * @return
     */
    public ProjectDTO updateProject(Long projectId, UpdateProjectRequest request){
        Project project = findProjectEntityyById(projectId);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project.setStartDate(request.getStartDate());
        project.setEndDate(request.getEndDate());
        Project updatedProject = projectRepository.save(project);
        return maptoProjectDTO(updatedProject);
    }

    /**
     *
     * @param projectId
     */
    public void deleteProject(Long projectId){
        Project project = findProjectEntityyById(projectId);
        projectRepository.delete(project);
    }

    /**
     *
     * @param projectId
     * @return
     */
    @Transactional(readOnly = true)
    public Project findProjectEntityWithUsersById(Long projectId) {
        return projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project", "id", projectId));
    }

    /**
     *
     * @param projectId
     * @param userIdsToAssign
     */
    @Transactional
    public void assignUsersToProject(Long projectId, Set<Long> userIdsToAssign) {
        Project project = findProjectEntityyById(projectId); // Pobierz projekt
        Set<User> currentUsers = new HashSet<>(project.getAssignedUsers());
        for (User user : currentUsers) {
            if (!userIdsToAssign.contains(user.getId())) {
                project.unassignUser(user);
            }
        }
        for (Long userId : userIdsToAssign) {

            boolean alreadyAssigned = project.getAssignedUsers().stream().anyMatch(u -> u.getId().equals(userId));
            if (!alreadyAssigned) {
                User user = userRepository.findById(userId)
                        .orElseThrow(() -> new ResourceNotFoundException("User", "id", userId));
                project.assignUser(user);
            }
        }
        projectRepository.save(project);
    }

    /**
     *
     * @param projectId
     * @return
     */
    @Transactional(readOnly = true)
    public Set<UserDTO> getAssignedUsersForProject(Long projectId) {
        Project project = findProjectEntityyById(projectId);
        return project.getAssignedUsers().stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toSet());
    }

    /**
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<UserDTO> getAllUsersAvailableForAssignment() {
        Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                .orElse(null);
        return userRepository.findAll().stream()
                .filter(user -> {
                    if (adminRole == null) return true;
                    return !user.getRoles().contains(adminRole);
                })
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    /**
     *
     * @return
     */
    @Transactional(readOnly = true)
    public List<ProjectDTO> getProjectsForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return List.of();
        }
        Object principal = authentication.getPrincipal();
        String username;
        if (principal instanceof UserDetails) {
            username = ((UserDetails) principal).getUsername();
        } else {
            username = principal.toString();
        }
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username + " (current)"));
        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) {
            return projectRepository.findAll().stream()
                    .map(this::maptoProjectDTO)
                    .collect(Collectors.toList());
        } else {
            return projectRepository.findProjectsByAssignedUserId(currentUser.getId()).stream()
                    .map(this::maptoProjectDTO)
                    .collect(Collectors.toList());
        }
    }
}

