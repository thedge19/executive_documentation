package com.executive_documentation.acts.service;

import com.executive_documentation.acts.dto.act.ActLogResponseDto;
import com.executive_documentation.acts.dto.act.ActResponseDto;
import com.executive_documentation.acts.dto.entrance.EntranceControlResponseDto;
import com.executive_documentation.acts.dto.materials.MaterialDto;
import com.executive_documentation.acts.dto.worklog.WorkLogDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.ExecutiveSchema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ActService {
    ActResponseDto get(Long id);

    ExecutiveSchema getExecutiveSchema(long id);

    List<ExecutiveSchema> getExecutiveSchemasAsc();

    List<ExecutiveSchema> getExecutiveSchemasDesc();

    List<ExecutiveSchema> getExecutiveSchemasFilteredByName();

    List<ActResponseDto> getAll();

    List<ActResponseDto> filterBySubObject();

    List<EntranceControlResponseDto> getAllEntranceControl();

    void create(Map<String, String> formData, MultipartFile file);

    ActResponseDto actUpdate(long id, String works, MultipartFile file);

    void delete(Long actId);

    void deleteSchema(long id);

    Map<String, Double> getActStats();

    long getGlobalStats();

    List<WorkLogDto> getWorkLog3();

    List<ActLogResponseDto> getWorkLog6();
}
