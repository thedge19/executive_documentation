package com.executive_documentation.projects.controller;

import com.executive_documentation.projects.model.Project;
import com.executive_documentation.projects.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost", "http://localhost:80", "http://frontend", "http://localhost:5173"})
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping("/{id}")
    public ResponseEntity<Project> getProject(@PathVariable Long id) {
        log.info("Getting project by id: {}", id);
        return projectService.get(id)
                .map(project -> {
                    log.info("Found project: {}", project);
                    return ResponseEntity.ok(project);
                })
                .orElseGet(() -> {
                    log.warn("Project not found with id: {}", id);
                    return ResponseEntity.notFound().build();
                });
    }

    @GetMapping
    public ResponseEntity<List<Project>> getAllProjects() {
        log.info("Getting all projects");
        List<Project> projects = projectService.getAll();
        log.info("Found {} projects", projects.size());
        return ResponseEntity.ok(projects);
    }

    @PostMapping
    public ResponseEntity<Project> createProject(@Valid @RequestBody Project project) {
        log.info("Creating project: {}", project.getName());
        Project createdProject = projectService.create(project);
        log.info("Created project with id: {}", createdProject.getId());
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createdProject);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Project> updateProject(
            @PathVariable Long id,
            @Valid @RequestBody Project project) {
        log.info("Updating project with id: {}", id);
        Project updatedProject = projectService.update(id, project);
        log.info("Updated project: {}", updatedProject);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProject(@PathVariable Long id) {
        log.info("Deleting project with id: {}", id);
        projectService.delete(id);
        log.info("Successfully deleted project with id: {}", id);
        return ResponseEntity.noContent().build();
    }
}
