package com.executive_documentation.workings.service;

import com.executive_documentation.workings.dto.*;
import com.executive_documentation.workings.model.Working;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface WorkingService {
    WorkingResponseDto get(Long id);

    List<WorkingResponseDto> getAll(long id);

    List<WorkingResponseDto> getAllByPositiveDone(long id);

    BigDecimal getTotalAmountBySubObject(long subObjectId);

    WorkingResponseDto create(WorkingRequestDto workingRequestDto);

    WorkingResponseDto update(long id, WorkingUpdateDto dto);

    void delete(long id);

    Working findWorkingOrNot(long id);

    Map<String, Long> getWorksCountBySubObject();

    Map<String, FinancialStats> getFinancialStatsBySubObject();

    TotalFinancialStats getTotalFinancialStats();

    int importFromExcel(Long subObjectId) throws IOException;
}
