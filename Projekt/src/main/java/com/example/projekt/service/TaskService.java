package com.example.projekt.service;


import com.example.projekt.Entity.Project;
import com.example.projekt.Entity.Task;
import com.example.projekt.dto.CreateTaskRequest;
import com.example.projekt.dto.TaskDTO;
import com.example.projekt.dto.UpdateTaskRequest;
import com.example.projekt.exception.ResourceNotFoundException;
import com.example.projekt.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectService projectService;

    /**
     *
     * @param task
     * @return
     */
    private TaskDTO mapToTaskDTO(Task task){
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        if(task.getProject() != null){
            dto.setProjectId(task.getProject().getId());
        }
        return dto;
    }

    /**
     *
     * @param projectId
     * @param request
     * @return
     */
    public TaskDTO createTask(Long projectId, CreateTaskRequest request){
        Project project = projectService.findProjectEntityyById(projectId);
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus() != null ? request.getStatus() : task.getStatus());
        task.setDueDate(request.getDueDate());
        project.addTask(task);
        Task savedTask = taskRepository.save(task);
        return mapToTaskDTO(savedTask);
    }

    /**
     *
     * @param projectId
     * @return
     */
    @Transactional(readOnly = true)
    public List<TaskDTO> getTasksByProjectId(Long projectId){
        projectService.findProjectEntityyById(projectId);
        List<Task> tasks = taskRepository.findByProjectId(projectId);
        return tasks.stream().map(this::mapToTaskDTO).collect(Collectors.toList());
    }

    /**
     *
     * @param taskId
     * @return
     */
    @Transactional(readOnly = true)
    public TaskDTO getTaskById(Long taskId){
        Task task = taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task", "id", taskId));
        return mapToTaskDTO(task);
    }

    /**
     *
     * @param taskId
     * @return
     */
    private Task findTaskEntityById(Long taskId){
        return taskRepository.findById(taskId).orElseThrow(() -> new ResourceNotFoundException("Task","id",taskId));
    }

    /**
     *
     * @param taskId
     * @param request
     * @return
     */
    public TaskDTO updateTask(Long taskId, UpdateTaskRequest request){
        Task task = findTaskEntityById(taskId);
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setStatus(request.getStatus());
        task.setDueDate(request.getDueDate());
        Task updatedTask = taskRepository.save(task);
        return mapToTaskDTO(updatedTask);
    }

    /**
     *
     * @param taskId
     */
    public void deleteTask(Long taskId){
        Task task = findTaskEntityById(taskId);
        taskRepository.delete(task);
    }
}
