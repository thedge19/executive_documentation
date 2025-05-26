package com.executive_documentation.materials.repository;

import com.executive_documentation.materials.model.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
}
