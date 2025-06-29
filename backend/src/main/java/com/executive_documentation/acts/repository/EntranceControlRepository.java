package com.executive_documentation.acts.repository;

import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EntranceControlRepository extends JpaRepository<EntranceControl, Long> {
    List<EntranceControl> findAllByAct(Act act);

    List<EntranceControl> findAllByOrderByDateAsc();

    @EntityGraph(attributePaths = {"act", "act.executiveSchema", "act.project", "materials"})
    @Query("SELECT ec FROM EntranceControl ec ORDER BY ec.date ASC")
    List<EntranceControl> findAllWithRelations();
}
