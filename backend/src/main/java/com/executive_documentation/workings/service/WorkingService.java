package com.executive_documentation.workings.service;



import com.executive_documentation.workings.dto.WorkingRequestDto;
import com.executive_documentation.workings.dto.WorkingUpdateDto;
import com.executive_documentation.workings.model.Working;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WorkingService {
    Working get(Long id);

    Page<Working> getAll(long id, Pageable pageable);

    List<Working> getAllByPositiveDone(long id);

    Working create(WorkingRequestDto workingRequestDto);

    Working update(long id, WorkingUpdateDto dto);

    void delete(long id);

    Working findWorkingOrNot(long id);
}
