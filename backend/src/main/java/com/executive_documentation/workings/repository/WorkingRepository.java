package com.executive_documentation.workings.repository;

import com.executive_documentation.workings.model.Working;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface WorkingRepository extends JpaRepository<Working, Long> {

    List<Working> findAllBySubObjectIdOrderByIdAsc(Long id);

    @Query("SELECT w FROM Working w WHERE w.subObject.id = :id AND w.finalQuantity > 0 ORDER BY w.name ASC")
    List<Working> findAllBySubObjectId(Long id);
}
