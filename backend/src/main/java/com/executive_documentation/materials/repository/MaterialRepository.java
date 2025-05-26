package com.executive_documentation.materials.repository;

import com.executive_documentation.materials.model.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    List<Material> findAllByOrderByName();
}
