package com.executive_documentation.projects.service;

import com.executive_documentation.projects.model.Project;
import com.executive_documentation.projects.repository.ProjectRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProjectServiceImplementation implements ProjectService {

    private final ProjectRepository projectRepository;

    @Override
    public Optional<Project> get(Long id) {
        return projectRepository.findById(id);
    }

    @Override
    public List<Project> getAll() {
        return projectRepository.findAll();
    }

    @Transactional
    @Override
    public Project create(Project project) {
        Objects.requireNonNull(project, "Project cannot be null");
        return projectRepository.save(project);
    }

    @Transactional
    @Override
    public Project update(Long id, Project project) {
        Objects.requireNonNull(project, "Project cannot be null");

        Project existingProject = projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));

        Optional.ofNullable(project.getName())
                .ifPresent(existingProject::setName);

        return projectRepository.save(existingProject);
    }

    @Transactional
    @Override
    public void delete(Long id) {
        if (!projectRepository.existsById(id)) {
            throw new EntityNotFoundException("Project not found with id: " + id);
        }
        projectRepository.deleteById(id);
    }

    @Override
    public Project findProjectOrThrow(Long id) {
        return projectRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Project not found with id: " + id));
    }
}