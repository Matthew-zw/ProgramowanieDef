package com.example.projekt.service;

import com.example.projekt.Entity.Project;
import com.example.projekt.Entity.Task;
import com.example.projekt.Entity.TaskStatus;
import com.example.projekt.dto.CreateTaskRequest;
import com.example.projekt.dto.TaskDTO;
import com.example.projekt.dto.UpdateTaskRequest;
import com.example.projekt.exception.ResourceNotFoundException;
import com.example.projekt.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private ProjectService projectService;

    @InjectMocks
    private TaskService taskService;

    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");

        testTask = new Task();
        testTask.setId(10L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Description of Test Task");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setDueDate(LocalDateTime.now().plusDays(7));
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUpdatedAt(LocalDateTime.now());
        testTask.setProject(testProject);
    }

    @Test
    void createTask_success() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");
        request.setDescription("New Description");
        request.setStatus(TaskStatus.IN_PROGRESS);
        request.setDueDate(LocalDateTime.now().plusDays(5));

        assertTrue(testProject.getTasks().isEmpty(), "Project should have no tasks initially");


        when(projectService.findProjectEntityyById(testProject.getId())).thenReturn(testProject);

        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task taskToSave = invocation.getArgument(0);
            if (taskToSave.getId() == null) {

            }
            return taskToSave;
        });

        TaskDTO createdTaskDto = taskService.createTask(testProject.getId(), request);

        assertNotNull(createdTaskDto);
        assertEquals("New Task", createdTaskDto.getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, createdTaskDto.getStatus());
        assertEquals(testProject.getId(), createdTaskDto.getProjectId());

        ArgumentCaptor<Task> taskCaptor = ArgumentCaptor.forClass(Task.class);
        verify(taskRepository, times(1)).save(taskCaptor.capture());

        Task savedTaskByService = taskCaptor.getValue();

        assertEquals(1, testProject.getTasks().size(), "Project should now have one task");
        assertTrue(testProject.getTasks().contains(savedTaskByService), "Project's task list should contain the newly created task");

        assertEquals("New Task", testProject.getTasks().get(0).getTitle());
    }

    @Test
    void createTask_projectNotFound() {
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("New Task");

        when(projectService.findProjectEntityyById(anyLong())).thenThrow(new ResourceNotFoundException("Project", "id", 99L));

        assertThrows(ResourceNotFoundException.class, () -> taskService.createTask(99L, request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTasksByProjectId_returnsTasks() {
        when(projectService.findProjectEntityyById(testProject.getId())).thenReturn(testProject);
        when(taskRepository.findByProjectId(testProject.getId())).thenReturn(Collections.singletonList(testTask));

        List<TaskDTO> tasks = taskService.getTasksByProjectId(testProject.getId());

        assertNotNull(tasks);
        assertEquals(1, tasks.size());
        assertEquals(testTask.getTitle(), tasks.get(0).getTitle());
        verify(projectService, times(1)).findProjectEntityyById(testProject.getId());
        verify(taskRepository, times(1)).findByProjectId(testProject.getId());
    }

    @Test
    void getTasksByProjectId_projectNotFound() {
        when(projectService.findProjectEntityyById(anyLong())).thenThrow(new ResourceNotFoundException("Project", "id", 99L));

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTasksByProjectId(99L));
        verify(taskRepository, never()).findByProjectId(anyLong());
    }

    @Test
    void getTaskById_found() {
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));

        TaskDTO foundTask = taskService.getTaskById(testTask.getId());

        assertNotNull(foundTask);
        assertEquals(testTask.getTitle(), foundTask.getTitle());
    }

    @Test
    void getTaskById_notFound() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.getTaskById(99L));
    }

    @Test
    void updateTask_success() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated Description");
        request.setStatus(TaskStatus.DONE);
        request.setDueDate(LocalDateTime.now().plusDays(10));

        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TaskDTO updatedTask = taskService.updateTask(testTask.getId(), request);

        assertNotNull(updatedTask);
        assertEquals("Updated Task", updatedTask.getTitle());
        assertEquals("Updated Description", updatedTask.getDescription());
        assertEquals(TaskStatus.DONE, updatedTask.getStatus());
        assertEquals(request.getDueDate(), updatedTask.getDueDate());
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    void updateTask_notFound() {
        UpdateTaskRequest request = new UpdateTaskRequest();
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.updateTask(99L, request));
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void deleteTask_success() {
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));

        taskService.deleteTask(testTask.getId());

        verify(taskRepository, times(1)).delete(testTask);
    }

    @Test
    void deleteTask_notFound() {
        when(taskRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> taskService.deleteTask(99L));
        verify(taskRepository, never()).delete(any(Task.class));
    }
}