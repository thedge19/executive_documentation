package com.executive_documentation.projects.controller;

import com.executive_documentation.projects.model.Project;
import com.executive_documentation.projects.service.ProjectService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectControllerTest {

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private ProjectController projectController;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
    }

    @Test
    void getProject_shouldReturnProject_whenProjectExists() {
        when(projectService.get(1L)).thenReturn(Optional.of(testProject));

        ResponseEntity<Project> response = projectController.getProject(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProject, response.getBody());
        verify(projectService).get(1L);
    }

    @Test
    void getProject_shouldReturnNotFound_whenProjectNotExists() {
        when(projectService.get(1L)).thenReturn(Optional.empty());

        ResponseEntity<Project> response = projectController.getProject(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(projectService).get(1L);
    }

    @Test
    void getAllProjects_shouldReturnAllProjects() {
        List<Project> projects = List.of(testProject);
        when(projectService.getAll()).thenReturn(projects);

        ResponseEntity<List<Project>> response = projectController.getAllProjects();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals(testProject, response.getBody().get(0));
        verify(projectService).getAll();
    }

    @Test
    void createProject_shouldCreateAndReturnProject() {
        when(projectService.create(testProject)).thenReturn(testProject);

        ResponseEntity<Project> response = projectController.createProject(testProject);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(testProject, response.getBody());
        verify(projectService).create(testProject);
    }

    @Test
    void updateProject_shouldUpdateAndReturnProject() {
        when(projectService.update(1L, testProject)).thenReturn(testProject);

        ResponseEntity<Project> response = projectController.updateProject(1L, testProject);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(testProject, response.getBody());
        verify(projectService).update(1L, testProject);
    }

    @Test
    void deleteProject_shouldDeleteProject() {
        doNothing().when(projectService).delete(1L);

        ResponseEntity<Void> response = projectController.deleteProject(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(projectService).delete(1L);
    }
}
