package com.example.projekt.Entity;


import nl.jqno.equalsverifier.EqualsVerifier;
import nl.jqno.equalsverifier.Warning;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ProjectTest {

    private Project project;
    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        project = new Project();
        project.setId(1L);
        project.setName("Main Project");

        task = new Task();
        task.setId(10L);
        task.setTitle("First Task");

        user = new User("testuser", "pass", "Test User", "test@user.com");
        user.setId(100L);
    }

    @Test
    void addTask_shouldSetBidirectionalRelationship() {
        assertTrue(project.getTasks().isEmpty());
        assertNull(task.getProject());

        project.addTask(task);

        assertEquals(1, project.getTasks().size());
        assertTrue(project.getTasks().contains(task));
        assertEquals(project, task.getProject());
    }

    @Test
    void removeTask_shouldBreakBidirectionalRelationship() {
        project.addTask(task);
        assertEquals(1, project.getTasks().size());

        project.removeTask(task);

        assertTrue(project.getTasks().isEmpty());
        assertNull(task.getProject());
    }

    @Test
    void assignUser_shouldSetBidirectionalRelationship() {
        assertTrue(project.getAssignedUsers().isEmpty());
        assertTrue(user.getProjects().isEmpty());

        project.assignUser(user);

        assertTrue(project.getAssignedUsers().contains(user));
        assertTrue(user.getProjects().contains(project));
    }

    @Test
    void unassignUser_shouldBreakBidirectionalRelationship() {
        project.assignUser(user);
        assertTrue(project.getAssignedUsers().contains(user));
        assertTrue(user.getProjects().contains(project));

        project.unassignUser(user);

        assertFalse(project.getAssignedUsers().contains(user));
        assertFalse(user.getProjects().contains(project));
    }
}