package com.example.projekt.controller;

import com.example.projekt.config.CustomAuthenticationSuccessHandler;
import com.example.projekt.dto.CreateProjectRequest;
import com.example.projekt.dto.ProjectDTO;
import com.example.projekt.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.example.projekt.exception.ResourceNotFoundException;
import static org.hamcrest.Matchers.is;

import com.example.projekt.config.SecurityConfig;
import com.example.projekt.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Import;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.hamcrest.Matchers.is;

@WebMvcTest(ProjectController.class)
@Import(SecurityConfig.class)
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProjectService projectService;

    @MockBean
    private UserDetailsServiceImpl userDetailsService;

    @MockBean
    private CustomAuthenticationSuccessHandler customAuthenticationSuccessHandler;

    private ProjectDTO testProjectDTO;

    @BeforeEach
    void setUp() {
        testProjectDTO = new ProjectDTO();
        testProjectDTO.setId(1L);
        testProjectDTO.setName("Test Project");
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser
    void listProjects_shouldReturnProjectsListView() throws Exception {
        List<ProjectDTO> projects = Collections.singletonList(testProjectDTO);
        when(projectService.getProjectsForCurrentUser()).thenReturn(projects);

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/list"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attribute("projects", projects));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void showCreateProjectForm_asManager_shouldReturnFormView() throws Exception {
        mockMvc.perform(get("/projects/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/form"))
                .andExpect(model().attributeExists("createProjectRequest"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "EMPLOYEE")
    void showCreateProjectForm_asEmployee_shouldBeForbidden() throws Exception {
        mockMvc.perform(get("/projects/new"))
                .andExpect(status().isForbidden());
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void createProject_withValidData_shouldRedirectToProjectsList() throws Exception {
        when(projectService.createProject(any(CreateProjectRequest.class))).thenReturn(testProjectDTO);

        mockMvc.perform(post("/projects")
                        .with(csrf())
                        .param("name", "New Valid Project")
                        .param("description", "A description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void createProject_withInvalidData_shouldReturnFormView() throws Exception {
        mockMvc.perform(post("/projects")
                        .with(csrf())
                        .param("name", "")
                        .param("description", "A description"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/form"))
                .andExpect(model().attributeHasFieldErrors("createProjectRequest", "name"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void deleteProject_asManager_shouldRedirect() throws Exception {
        mockMvc.perform(post("/projects/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void getProjectById_whenNotFound_shouldReturn404AndErrorMessage() throws Exception {
        when(projectService.getProjectById(99L)).thenThrow(new ResourceNotFoundException("Project", "id", 99L));

        mockMvc.perform(get("/projects/edit/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", is("Project not found with id : '99'")));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void createProject_withInvalidData_shouldTriggerValidation() throws Exception {
        mockMvc.perform(post("/projects").with(csrf())
                        .param("name", "")
                        .param("description", "A description"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/form"))
                .andExpect(model().attributeHasFieldErrors("createProjectRequest", "name"));
    }

    /**
     *
     * @throws Exception
     */
    @Test
    @WithMockUser(roles = "PROJECT_MANAGER")
    void getProjectById_whenNotFound_shouldTriggerExceptionHandler() throws Exception {
        when(projectService.getProjectById(99L)).thenThrow(new ResourceNotFoundException("Project", "id", 99L));


        mockMvc.perform(get("/projects/edit/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message", is("Project not found with id : '99'")));
    }

}