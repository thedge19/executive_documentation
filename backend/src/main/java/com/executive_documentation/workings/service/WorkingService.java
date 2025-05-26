package com.executive_documentation.workings.service;



import com.executive_documentation.workings.dto.WorkingRequestDto;
import com.executive_documentation.workings.dto.WorkingUpdateDto;
import com.executive_documentation.workings.model.Working;

import java.util.List;

public interface WorkingService {
    Working get(Long id);

    List<Working> getAll(long id);

    List<Working> getAllByPositiveDone(long id);

    Working create(WorkingRequestDto workingRequestDto);

    Working update(long id, WorkingUpdateDto dto);

    void delete(long id);

    Working findWorkingOrNot(long id);
}
