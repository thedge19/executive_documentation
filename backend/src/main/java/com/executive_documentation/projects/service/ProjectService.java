package com.executive_documentation.projects.service;

import com.executive_documentation.projects.model.Project;

import java.util.List;
import java.util.Optional;

public interface ProjectService {
    Optional<Project> get(Long id);

    List<Project> getAll();

    Project create(Project project);

    Project update(Long id, Project project);

    void delete(Long id);

    Project findProjectOrThrow(Long id);
}
