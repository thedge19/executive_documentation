package com.executive_documentation.acts.repository;

import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.ExecutiveSchema;
import com.executive_documentation.subobjects.model.SubObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ActRepository extends JpaRepository<Act, Long> {
    Long countBySubObject(SubObject subObject);

    List<Act> findAllByEndDateBetweenOrderByEndDateAscActNumberAsc(LocalDate startDate, LocalDate endDate);

    List<Act> findAllByOrderByEndDateAscActNumberAsc();

    List<Act> findAllByOrderByStartDateAscActNumberAsc();

    List<Act> findAllByOrderByActNumberAsc();

    Act findByActNumber(String actNumber);

    @Query("SELECT a FROM Act a WHERE a.inRegistry is null ORDER BY a.endDate ASC, a.actNumber ASC")
    List<Act> findAllByOrderByEndDateAsc();

    @Query("SELECT a.executiveSchema.id FROM Act a WHERE a.id = :actId")
    Optional<Long> findExecutiveSchemaIdByActId(@Param("actId") Long actId);

    Optional<Act> findByExecutiveSchema(ExecutiveSchema executiveSchema);

    List<Act> findByEndDateBetween(LocalDate start, LocalDate end);
}
