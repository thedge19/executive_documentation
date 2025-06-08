package com.executive_documentation.acts.service;

import com.executive_documentation.acts.dto.ActResponseDto;
import com.executive_documentation.acts.dto.EntranceControlResponseDto;
import com.executive_documentation.acts.model.Act;
import com.executive_documentation.acts.model.ExecutiveSchema;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface ActService {
    ActResponseDto get(Long id);

    Act findActOrThrow(long id);

    ExecutiveSchema getExecutiveSchema(long id);

    List<ExecutiveSchema> getExecutiveSchemas();

//    ActUpdateResponseDto getUpdatedAct(long id);

    List<ActResponseDto> getAll();

//    List<ActResponseDto> findAllByEndDateBetween(LocalDate startDate, LocalDate endDate);

    List<EntranceControlResponseDto> getAllEntranceControl();

    void create(Map<String, String> formData, MultipartFile file);

    ActResponseDto actUpdate(long id, MultipartFile file);

//    Act update(long id, ActUpdateRequestDto requestDto);

    void delete(Long actId);

    void deleteSchema(long id);

//    void deleteControl(long id);

//    Act findActOrNot(long id);

//    EntranceControl findEntranceControl(long id);

//    List<EntranceControl> controls(Act act);

//    EntranceControl updateEntranceControl(long id, EntranceControlRequestDto requestDto);

//    LocalDate jsDateToLocalDate(String date);

//    List<ActResponseDto> filterBySubObject();

//    List<ActResponseDto> getAllWithNullInRegistries();



}
