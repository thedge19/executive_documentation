package com.executive_documentation.materials.repository;

import com.executive_documentation.materials.model.Material;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    @EntityGraph(attributePaths = {"certificates"})
    @Query("SELECT m FROM Material m WHERE m.id = :id")
    Optional<Material> findByIdWithCertificates(@Param("id") Long id);

//    @EntityGraph(attributePaths = {"certificates"})
//    @Query("SELECT DISTINCT m FROM Material m LEFT JOIN m.certificates")
//    Set<Material> findAllWithCertificates(); // Возвращаем Set

    List<Material> findAllByOrderByName();

//    @EntityGraph(attributePaths = {"certificates"})
//    @Query("SELECT DISTINCT m FROM Material m LEFT JOIN FETCH m.certificates WHERE m.id IN :materialIds")
//    List<Material> findAllWithCertificatesByIdIn(@Param("materialIds") List<Long> materialIds);
}
