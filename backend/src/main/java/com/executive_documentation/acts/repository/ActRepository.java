package com.executive_documentation.acts.repository;

import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.ExecutiveSchema;
import com.executive_documentation.subobjects.model.SubObject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ActRepository extends JpaRepository<Act, Long> {
    Long countBySubObject(SubObject subObject);

    List<Act> findAllByOrderByEndDateAscActNumberAsc();

    List<Act> findAllByOrderByStartDateAscActNumberAsc();

    List<Act> findAllByOrderByActNumberAsc();

    Optional<Act> findByExecutiveSchema(ExecutiveSchema executiveSchema);

    List<Act> findByEndDateBetween(LocalDate start, LocalDate end);

    @Query("SELECT a.subObject.title, COUNT(a) FROM Act a GROUP BY a.subObject.title")
    List<Object[]> countActsBySubObjectTitle();

    @Query("SELECT COUNT(a) FROM Act a")
    long countAllActs();
}
