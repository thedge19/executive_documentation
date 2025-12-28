package com.executive_documentation.acts.repository;

import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.ExecutiveSchema;
import com.executive_documentation.subobjects.model.SubObject;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActRepository extends JpaRepository<Act, Long> {
    Long countBySubObject(SubObject subObject);

    @EntityGraph(attributePaths = {"executiveSchema", "project"})
    List<Act> findAllByOrderByEndDateAscActNumberAsc();

    @EntityGraph(attributePaths = {"executiveSchema", "project"})
    List<Act> findAllByOrderByStartDateAscActNumberAsc();

    List<Act> findAllByOrderByActNumberAsc();

    Optional<Act> findByExecutiveSchema(ExecutiveSchema executiveSchema);

    @Query("SELECT a FROM Act a WHERE a.endDate BETWEEN :start AND :end ORDER BY a.endDate ASC, a.actNumber ASC")
    List<Act> findByEndDateBetween(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end
    );

    @Query("SELECT a FROM Act a WHERE a.id IN :ids ORDER BY a.endDate ASC, a.actNumber ASC")
    List<Act> findAllByIdInOrderByEndDateAscActNumberAsc(List<Long> ids);

    @Query("SELECT a.subObject.title, COUNT(a) FROM Act a GROUP BY a.subObject.title")
    List<Object[]> countActsBySubObjectTitle();

    @Query("SELECT COUNT(a) FROM Act a")
    long countAllActs();

    @Query("SELECT DISTINCT a FROM Act a " +
            "JOIN a.materials am " +
            "WHERE am IS NULL " +
            "ORDER BY a.startDate")
    List<Act> findActsWithNonEmptyMaterialsOrderByStartDate();
}
