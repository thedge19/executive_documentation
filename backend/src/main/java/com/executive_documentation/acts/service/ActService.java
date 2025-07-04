package com.executive_documentation.acts.service;

import com.executive_documentation.acts.dto.act.ActResponseDto;
import com.executive_documentation.acts.dto.entrance.EntranceControlResponseDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.ExecutiveSchema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ActService {
    ActResponseDto get(Long id);

    Act findActOrThrow(long id);

    ExecutiveSchema getExecutiveSchema(long id);

    List<ExecutiveSchema> getExecutiveSchemasAsc();

    List<ExecutiveSchema> getExecutiveSchemasDesc();

    List<ExecutiveSchema> getExecutiveSchemasFilteredByName();

    List<ActResponseDto> getAll();

    List<ActResponseDto> filterBySubObject();

    List<EntranceControlResponseDto> getAllEntranceControl();

    void create(Map<String, String> formData, MultipartFile file);

    ActResponseDto actUpdate(long id, MultipartFile file);

    void delete(Long actId);

    void deleteSchema(long id);

    Map<String, Double> getActStats();

    long getGlobalStats();
}
