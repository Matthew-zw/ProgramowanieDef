package com.example.projekt.service;

import com.example.projekt.Entity.Project;
import com.example.projekt.Entity.Role;
import com.example.projekt.Entity.User;
import com.example.projekt.dto.CreateProjectRequest;
import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.dto.UpdateProjectRequest;
import com.example.projekt.dto.UserDTO;
import com.example.projekt.exception.ResourceNotFoundException;
import com.example.projekt.repository.ProjectRepository;
import com.example.projekt.repository.RoleRepository;
import com.example.projekt.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private Authentication authentication;
    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private User managerUser;
    private User employeeUser;
    private User adminUser;
    private Role managerRole;
    private Role employeeRole;
    private Role adminRole;

    @BeforeEach
    void setUp() {
        managerRole = new Role("ROLE_PROJECT_MANAGER");
        managerRole.setId(10L);
        employeeRole = new Role("ROLE_EMPLOYEE");
        employeeRole.setId(11L);
        adminRole = new Role("ROLE_ADMIN");
        adminRole.setId(12L);

        managerUser = new User("manager", "pass", "Manager User", "manager@example.com");
        managerUser.setId(1L);
        managerUser.setRoles(new HashSet<>(Arrays.asList(managerRole, employeeRole)));

        employeeUser = new User("employee", "pass", "Employee User", "employee@example.com");
        employeeUser.setId(2L);
        employeeUser.setRoles(new HashSet<>(Collections.singletonList(employeeRole)));

        adminUser = new User("admin", "pass", "Admin User", "admin@example.com");
        adminUser.setId(3L);
        adminUser.setRoles(new HashSet<>(Collections.singletonList(adminRole)));

        testProject = new Project();
        testProject.setId(100L);
        testProject.setName("Test Project");
        testProject.setDescription("Description for Test Project");
        testProject.setStartDate(LocalDate.of(2023, 1, 1));
        testProject.setEndDate(LocalDate.of(2023, 12, 31));
        testProject.setAssignedUsers(new HashSet<>());

        SecurityContextHolder.setContext(securityContext);
        lenient().when(securityContext.getAuthentication()).thenReturn(authentication);
    }

    /**
     *
     * @param username
     * @param roles
     */
    private void mockAuthentication(String username, String... roles) {
        List<SimpleGrantedAuthority> authorities = Arrays.stream(roles)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn(username);

        lenient().doReturn(authorities).when(userDetails).getAuthorities();
        lenient().doReturn(authorities).when(authentication).getAuthorities();

        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        lenient().when(authentication.getName()).thenReturn(username);

        if (username.equals(managerUser.getUsername())) {
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(managerUser));
        } else if (username.equals(employeeUser.getUsername())) {
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(employeeUser));
        } else if (username.equals(adminUser.getUsername())) {
            when(userRepository.findByUsername(username)).thenReturn(Optional.of(adminUser));
        } else {
            when(userRepository.findByUsername(username)).thenReturn(Optional.empty());
        }
    }

    @Test
    void createProject_byManager_assignsCreator() {
        mockAuthentication(managerUser.getUsername(), "ROLE_PROJECT_MANAGER");
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project");
        request.setDescription("New Description");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(101L);
            return p;
        });

        ProjectDTO createdProject = projectService.createProject(request);

        assertNotNull(createdProject);
        assertEquals("New Project", createdProject.getName());

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, times(1)).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertTrue(savedProject.getAssignedUsers().contains(managerUser), "Manager should be assigned to the project.");
    }

    @Test
    void createProject_byEmployee_doesNotAssignCreator() {
        mockAuthentication(employeeUser.getUsername(), "ROLE_EMPLOYEE");
        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("New Project 2");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(102L);
            return p;
        });

        ProjectDTO createdProject = projectService.createProject(request);

        assertNotNull(createdProject);
        assertEquals("New Project 2", createdProject.getName());

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, times(1)).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertTrue(savedProject.getAssignedUsers().isEmpty(), "No users should be assigned by a non-manager employee.");
    }

    @Test
    void createProject_byAnonymousUser_noCreatorAssigned() {
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getPrincipal()).thenReturn("anonymousUser");
        lenient(). when(authentication.getName()).thenReturn("anonymousUser");
        lenient().when(authentication.getAuthorities()).thenReturn(Collections.emptyList());

        CreateProjectRequest request = new CreateProjectRequest();
        request.setName("Anon Project");

        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project p = invocation.getArgument(0);
            p.setId(103L);
            return p;
        });

        ProjectDTO createdProject = projectService.createProject(request);

        assertNotNull(createdProject);
        assertEquals("Anon Project", createdProject.getName());

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository, times(1)).save(projectCaptor.capture());
        Project savedProject = projectCaptor.getValue();

        assertTrue(savedProject.getAssignedUsers().isEmpty(), "No users should be assigned by an anonymous user.");
    }


    @Test
    void getAllProjects_returnsAllProjects() {
        List<Project> projects = Arrays.asList(testProject, new Project());
        when(projectRepository.findAll()).thenReturn(projects);

        List<ProjectDTO> projectDTOS = projectService.getAllProjects();
        assertNotNull(projectDTOS);
        assertEquals(2, projectDTOS.size());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    void getProjectById_found() {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(testProject));
        ProjectDTO projectDTO = projectService.getProjectById(100L);
        assertNotNull(projectDTO);
        assertEquals(testProject.getName(), projectDTO.getName());
    }

    @Test
    void getProjectById_notFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectById(999L));
    }

    @Test
    void updateProject_success() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        request.setName("Updated Project Name");
        request.setDescription("Updated Description");
        request.setStartDate(LocalDate.of(2023, 2, 1));
        request.setEndDate(LocalDate.of(2023, 11, 30));

        when(projectRepository.findById(100L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        ProjectDTO updatedProject = projectService.updateProject(100L, request);

        assertNotNull(updatedProject);
        assertEquals("Updated Project Name", updatedProject.getName());
        assertEquals("Updated Description", updatedProject.getDescription());
        assertEquals(LocalDate.of(2023, 2, 1), updatedProject.getStartDate());
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void updateProject_notFound() {
        UpdateProjectRequest request = new UpdateProjectRequest();
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.updateProject(999L, request));
    }

    @Test
    void deleteProject_success() {
        when(projectRepository.findById(100L)).thenReturn(Optional.of(testProject));
        projectService.deleteProject(100L);
        verify(projectRepository, times(1)).delete(testProject);
    }

    @Test
    void deleteProject_notFound() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> projectService.deleteProject(999L));
        verify(projectRepository, never()).delete(any(Project.class));
    }

    @Test
    void assignUsersToProject_addUsers() {
        testProject.getAssignedUsers().clear();
        testProject.getAssignedUsers().add(managerUser);
        Set<Long> userIdsToAssign = new HashSet<>(Arrays.asList(managerUser.getId(), employeeUser.getId()));

        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(userRepository.findById(employeeUser.getId())).thenReturn(Optional.of(employeeUser));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        projectService.assignUsersToProject(testProject.getId(), userIdsToAssign);

        assertEquals(2, testProject.getAssignedUsers().size());
        assertTrue(testProject.getAssignedUsers().contains(managerUser));
        assertTrue(testProject.getAssignedUsers().contains(employeeUser));
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void assignUsersToProject_removeUsers() {
        testProject.getAssignedUsers().clear();
        testProject.getAssignedUsers().add(managerUser);
        testProject.getAssignedUsers().add(employeeUser);
        Set<Long> userIdsToAssign = new HashSet<>(Collections.singletonList(managerUser.getId()));

        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class))).thenReturn(testProject);

        projectService.assignUsersToProject(testProject.getId(), userIdsToAssign);

        assertEquals(1, testProject.getAssignedUsers().size());
        assertTrue(testProject.getAssignedUsers().contains(managerUser));
        assertFalse(testProject.getAssignedUsers().contains(employeeUser));
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    void assignUsersToProject_projectNotFound() {
        Set<Long> userIdsToAssign = Collections.singleton(employeeUser.getId());
        when(projectRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.assignUsersToProject(999L, userIdsToAssign));
        verify(projectRepository, never()).save(any(Project.class));
    }

    @Test
    void assignUsersToProject_userNotFoundWhenAdding() {
        testProject.getAssignedUsers().clear();
        Set<Long> userIdsToAssign = Collections.singleton(999L);
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.assignUsersToProject(testProject.getId(), userIdsToAssign));
        verify(projectRepository, never()).save(any(Project.class));
    }


    @Test
    void getAssignedUsersForProject_returnsUsers() {
        testProject.getAssignedUsers().add(managerUser);
        testProject.getAssignedUsers().add(employeeUser);
        when(projectRepository.findById(testProject.getId())).thenReturn(Optional.of(testProject));

        Set<UserDTO> assignedUsers = projectService.getAssignedUsersForProject(testProject.getId());

        assertNotNull(assignedUsers);
        assertEquals(2, assignedUsers.size());
        assertTrue(assignedUsers.stream().anyMatch(u -> u.getId().equals(managerUser.getId())));
        assertTrue(assignedUsers.stream().anyMatch(u -> u.getId().equals(employeeUser.getId())));
    }

    @Test
    void getAllUsersAvailableForAssignment_filtersAdmin() {
        when(userRepository.findAll()).thenReturn(Arrays.asList(managerUser, employeeUser, adminUser));
        when(roleRepository.findByName("ROLE_ADMIN")).thenReturn(Optional.of(adminRole));

        List<UserDTO> availableUsers = projectService.getAllUsersAvailableForAssignment();

        assertNotNull(availableUsers);
        assertEquals(2, availableUsers.size());
        assertTrue(availableUsers.stream().anyMatch(u -> u.getId().equals(managerUser.getId())));
        assertTrue(availableUsers.stream().anyMatch(u -> u.getId().equals(employeeUser.getId())));
        assertFalse(availableUsers.stream().anyMatch(u -> u.getId().equals(adminUser.getId())));
    }

    @Test
     void getProjectsForCurrentUser_adminUser_returnsAllProjects() {
        mockAuthentication(adminUser.getUsername(), "ROLE_ADMIN");
        List<Project> allProjects = Arrays.asList(testProject, new Project());
        when(projectRepository.findAll()).thenReturn(allProjects);

        List<ProjectDTO> projects = projectService.getProjectsForCurrentUser();

        assertNotNull(projects);
        assertEquals(2, projects.size());
        verify(projectRepository, times(1)).findAll();
        verify(projectRepository, never()).findProjectsByAssignedUserId(anyLong());
    }

    @Test
    void getProjectsForCurrentUser_assignedEmployee_returnsAssignedProjects() {
        mockAuthentication(employeeUser.getUsername(), "ROLE_EMPLOYEE");
        testProject.getAssignedUsers().add(employeeUser);
        List<Project> assignedProjects = Collections.singletonList(testProject);
        when(projectRepository.findProjectsByAssignedUserId(employeeUser.getId())).thenReturn(assignedProjects);


        List<ProjectDTO> projects = projectService.getProjectsForCurrentUser();

        assertNotNull(projects);
        assertEquals(1, projects.size());
        assertEquals(testProject.getName(), projects.get(0).getName());
        verify(projectRepository, times(1)).findProjectsByAssignedUserId(employeeUser.getId());
        verify(projectRepository, never()).findAll();
    }

    @Test
    void getProjectsForCurrentUser_unauthenticated_returnsEmptyList() {
        when(securityContext.getAuthentication()).thenReturn(null);

        List<ProjectDTO> projects = projectService.getProjectsForCurrentUser();

        assertNotNull(projects);
        assertTrue(projects.isEmpty());
        verify(userRepository, never()).findByUsername(anyString());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, never()).findProjectsByAssignedUserId(anyLong());
    }

    @Test
    void getProjectsForCurrentUser_userNotFound_throwsException() {
        mockAuthentication("nonexistent", "ROLE_EMPLOYEE");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> projectService.getProjectsForCurrentUser());
    }

    @Test
    void getProjectsForCurrentUser_unauthenticatedUser_returnsEmptyList() {
        // Symulujemy brak zalogowanego użytkownika
        when(securityContext.getAuthentication()).thenReturn(null);

        List<ProjectDTO> projects = projectService.getProjectsForCurrentUser();

        assertTrue(projects.isEmpty());
        verify(projectRepository, never()).findAll();
        verify(projectRepository, never()).findProjectsByAssignedUserId(anyLong());
    }
}