package com.executive_documentation.subobjects.repository;

import com.executive_documentation.subobjects.model.SubObject;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubObjectRepository extends JpaRepository<SubObject, Long> {
    List<SubObject> findAllByProjectIdOrderByIdAsc(Long projectId);
}
