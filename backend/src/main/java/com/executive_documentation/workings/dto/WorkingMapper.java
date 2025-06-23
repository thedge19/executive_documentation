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
                .unitPrice(working.getUnitPrice())
                .finalQuantity(working.getFinalQuantity())
                .doneAmount(working.getDoneAmount())
                .remainingAmount(working.getRemainingAmount())
                .subObjectId(working.getSubObject() != null ? working.getSubObject().getId() : null)
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

    public Working updateDtoToEntity(WorkingUpdateDto updateDto, Working existingEntity) {

        if (updateDto.getName() != null) {
            existingEntity.setName(updateDto.getName());
        }
        if (updateDto.getUnits() != null) {
            existingEntity.setUnits(updateDto.getUnits());
        }
        if (updateDto.getQuantity() != null) {
            existingEntity.setQuantity(updateDto.getQuantity());
        }
        if (updateDto.getDone() != null) {
            existingEntity.setDone(updateDto.getDone());
        }
        if (updateDto.getStandardId() != null) {
            existingEntity.setStandard(Standard.builder().id(updateDto.getStandardId()).build());
        }
        if (updateDto.getUnitPrice() != null) {
            existingEntity.setUnitPrice(updateDto.getUnitPrice());
        }

        return existingEntity;
    }
}
