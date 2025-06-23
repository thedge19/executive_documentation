package com.executive_documentation.exception.repository;

import com.executive_documentation.exception.model.ErrorLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface ErrorLogRepository extends JpaRepository<ErrorLog, Long> {
    List<ErrorLog> findAllByOrderByCreatedAtDesc();

    @Query("SELECT COUNT(e) FROM ErrorLog e")
    long countAllErrors();

    @Query("SELECT COUNT(e) FROM ErrorLog e WHERE e.createdAt >= :since")
    long countErrorsSince(LocalDateTime since);

    @Query("SELECT e.level, COUNT(e) FROM ErrorLog e GROUP BY e.level")
    List<Object[]> countByErrorLevel();

    @Query("SELECT FUNCTION('DATE', e.createdAt), COUNT(e) FROM ErrorLog e " +
            "WHERE e.createdAt >= :since GROUP BY FUNCTION('DATE', e.createdAt)")
    List<Object[]> countByDaySince(LocalDateTime since);

//    @Query("SELECT e.message, COUNT(e) FROM ErrorLog e GROUP BY e.message ORDER BY COUNT(e) DESC LIMIT 1")
//    Object[] findMostCommonErrorMessage();

    @Query("SELECT e.endpoint, COUNT(e) as cnt FROM ErrorLog e GROUP BY e.endpoint ORDER BY cnt DESC")
    List<Object[]> findMostFrequentEndpoint();

    @Query("SELECT e.message, COUNT(e) as cnt FROM ErrorLog e GROUP BY e.message ORDER BY cnt DESC")
    List<Object[]> findMostCommonMessage();
}
