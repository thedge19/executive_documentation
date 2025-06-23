package com.executive_documentation.workings.dto;

import com.executive_documentation.standard.model.Standard;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.workings.model.Working;
import org.springframework.stereotype.Component;

@Component
public class WorkingMapper {

    public WorkingResponseDto toDto(Working working) {
        return WorkingResponseDto.builder()
                .id(working.getId())
                .name(working.getName())
                .units(working.getUnits())
                .quantity(working.getQuantity())
                .done(working.getDone())
                .standardId(working.getStandard() != null ? working.getStandard().getId() : null)
                .finalQuantity(working.getFinalQuantity())
                .build();
    }

    public Working toEntity(WorkingRequestDto workingDto) {
        return Working.builder()
                .name(workingDto.getName())
                .units(workingDto.getUnits())
                .quantity(workingDto.getQuantity())
                .done(workingDto.getDone())
                .standard(Standard.builder().id(workingDto.getStandardId()).build())
                .subObject(SubObject.builder().id(workingDto.getSubObjectId()).build())
                .build();
    }
}
