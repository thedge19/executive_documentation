package com.executive_documentation.materials.repository;

import com.executive_documentation.materials.model.Certificate;
import com.executive_documentation.materials.model.Material;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    @EntityGraph(attributePaths = {"material"})
    List<Certificate> findAllByMaterialIdIn(List<Long> materialIds);

    @Modifying
    @Query("DELETE FROM Certificate c WHERE c.material.id = :materialId")
    void deleteAllByMaterialId(@Param("materialId") Long materialId);

    List<Certificate> findAllByMaterial(Material material);

    Certificate findCertificateByMaterial(Material material);
}
