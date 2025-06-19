package com.executive_documentation.acts.repository;

import com.executive_documentation.acts.model.ExecutiveSchema;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExecutiveSchemaRepository extends JpaRepository<ExecutiveSchema, Long> {
    List<ExecutiveSchema> findAllByOrderBySchemasActNumberAsc();
    List<ExecutiveSchema> findAllByOrderBySchemasActNumberDesc();
}
