package com.executive_documentation.acts.repository;

import com.executive_documentation.acts.model.ExecutiveSchema;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExecutiveSchemaRepository extends JpaRepository<ExecutiveSchema, Long> {
    ExecutiveSchema findBySchemasActNumber(String executiveName);
}
