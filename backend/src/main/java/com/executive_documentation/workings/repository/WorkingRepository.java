package com.executive_documentation.workings.repository;

import com.executive_documentation.workings.model.Working;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface WorkingRepository extends JpaRepository<Working, Long> {

    Page<Working> findAllBySubObjectIdOrderByIdAsc(Long id, Pageable pageable);

    @Query("SELECT w FROM Working w WHERE w.subObject.id = :id AND w.finalQuantity > 0 ORDER BY w.name ASC")
    List<Working> findAllBySubObjectId(Long id);

    @Query("SELECT w.subObject.title, COUNT(w) FROM Working w GROUP BY w.subObject.title")
    List<Object[]> countWorksBySubObjectTitle();

    @Transactional
    @Modifying
    @Query("DELETE FROM Working w WHERE w.subObject.id = :subObjectId")
    void deleteAllBySubObjectId(@Param("subObjectId") Long subObjectId);

    @Query("SELECT COUNT(w) FROM Working w")
    long countAllWorkings();
}
