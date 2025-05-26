package com.executive_documentation.projects.repository;

import com.executive_documentation.projects.model.Project;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProjectRepository extends JpaRepository<Project, Long> {
}
