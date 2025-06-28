package com.example.projekt.repository;

import com.example.projekt.Entity.Project;
import com.example.projekt.Entity.Task;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void whenTaskIsPersisted_thenOnCreateCallbackIsTriggered() {
        Project project = new Project();
        project.setName("Project for Task");
        entityManager.persist(project);

        Task task = new Task();
        task.setTitle("Test PrePersist");
        task.setProject(project);

        Task savedTask = taskRepository.save(task);

        assertNotNull(savedTask.getCreatedAt());
        assertNotNull(savedTask.getUpdatedAt());
    }
}