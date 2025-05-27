package com.executive_documentation.materials.repository;

import com.executive_documentation.materials.model.Material;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    Page<Material> findAllByOrderByName(Pageable pageable);
}
