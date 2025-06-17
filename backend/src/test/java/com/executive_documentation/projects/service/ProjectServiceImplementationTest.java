package com.executive_documentation.projects.service;

import com.executive_documentation.projects.model.Project;
import com.executive_documentation.projects.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplementationTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectServiceImplementation projectService;

    private Project testProject;

    @BeforeEach
    void setUp() {
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Test Project");
    }

    @Test
    void get_shouldReturnProject_whenProjectExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        Optional<Project> result = projectService.get(1L);

        assertTrue(result.isPresent());
        assertEquals(testProject, result.get());
        verify(projectRepository).findById(1L);
    }

    @Test
    void get_shouldReturnEmptyOptional_whenProjectNotExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Project> result = projectService.get(1L);

        assertTrue(result.isEmpty());
        verify(projectRepository).findById(1L);
    }

    @Test
    void getAll_shouldReturnAllProjects() {
        List<Project> projects = List.of(testProject);
        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectService.getAll();

        assertEquals(1, result.size());
        assertEquals(testProject, result.getFirst());
        verify(projectRepository).findAll();
    }

    @Test
    void create_shouldSaveAndReturnProject() {
        when(projectRepository.save(testProject)).thenReturn(testProject);

        Project result = projectService.create(testProject);

        assertEquals(testProject, result);
        verify(projectRepository).save(testProject);
    }

    @Test
    void create_shouldThrowException_whenProjectIsNull() {
        assertThrows(NullPointerException.class, () -> projectService.create(null));
        verifyNoInteractions(projectRepository);
    }

    @Test
    void update_shouldUpdateExistingProject() {
        Project updatedProject = new Project();
        updatedProject.setName("Updated Name");

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);

        Project result = projectService.update(1L, updatedProject);

        assertEquals("Updated Name", result.getName());
        verify(projectRepository).findById(1L);
        verify(projectRepository).save(testProject);
    }

    @Test
    void update_shouldThrowException_whenProjectNotExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> projectService.update(1L, testProject));

        verify(projectRepository).findById(1L);
        verify(projectRepository, never()).save(any());
    }

    @Test
    void update_shouldNotUpdateName_whenNameIsNull() {
        Project updatedProject = new Project();

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(testProject)).thenReturn(testProject);

        Project result = projectService.update(1L, updatedProject);

        assertEquals("Test Project", result.getName());
        verify(projectRepository).findById(1L);
        verify(projectRepository).save(testProject);
    }

    @Test
    void delete_shouldDeleteProject_whenProjectExists() {
        when(projectRepository.existsById(1L)).thenReturn(true);

        projectService.delete(1L);

        verify(projectRepository).existsById(1L);
        verify(projectRepository).deleteById(1L);
    }

    @Test
    void delete_shouldThrowException_whenProjectNotExists() {
        when(projectRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class,
                () -> projectService.delete(1L));

        verify(projectRepository).existsById(1L);
        verify(projectRepository, never()).deleteById(any());
    }

    @Test
    void findProjectOrThrow_shouldReturnProject_whenProjectExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        Project result = projectService.findProjectOrThrow(1L);

        assertEquals(testProject, result);
        verify(projectRepository).findById(1L);
    }

    @Test
    void findProjectOrThrow_shouldThrowException_whenProjectNotExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class,
                () -> projectService.findProjectOrThrow(1L));

        verify(projectRepository).findById(1L);
    }
}
