package com.executive_documentation.acts.repository;

import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.EntranceControl;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EntranceControlRepository extends JpaRepository<EntranceControl, Long> {
    List<EntranceControl> findAllByAct(Act act);

    List<EntranceControl> findAllByOrderByDateAsc();
}
