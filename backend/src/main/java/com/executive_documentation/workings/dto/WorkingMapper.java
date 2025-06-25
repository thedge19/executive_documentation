package com.executive_documentation.workings.dto;

import com.executive_documentation.standard.model.Standard;
import com.executive_documentation.standard.repository.StandardRepository;
import com.executive_documentation.subobjects.model.SubObject;
import com.executive_documentation.subobjects.repository.SubObjectRepository;
import com.executive_documentation.workings.model.Working;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Component
public class WorkingMapper {

    private final SubObjectRepository subObjectRepository;
    private final StandardRepository standardRepository;

    public WorkingResponseDto toDto(Working working) {
        BigDecimal done = working.getDone();
        BigDecimal quantity = working.getQuantity();
        BigDecimal unitPrice = working.getUnitPrice();

        BigDecimal doneAmount;
        BigDecimal remainingAmount;

        // Если выполнено больше, чем запланировано (done > quantity)
        if (done != null && quantity != null && done.compareTo(quantity) > 0) {
            doneAmount = quantity.multiply(unitPrice);  // doneAmount = quantity * unitPrice
            remainingAmount = BigDecimal.ZERO;          // remainingAmount = 0
        } else {
            // Иначе берём значения из сущности (или null, если они не заданы)
            doneAmount = working.getDoneAmount();
            remainingAmount = working.getRemainingAmount();
        }

        return WorkingResponseDto.builder()
                .id(working.getId())
                .name(working.getName())
                .units(working.getUnits())
                .quantity(quantity)
                .done(done)
                .standardId(working.getStandard() != null ? working.getStandard().getId() : null)
                .unitPrice(unitPrice)
                .finalQuantity(working.getFinalQuantity())
                .doneAmount(doneAmount)
                .remainingAmount(remainingAmount)
                .subObjectId(working.getSubObject() != null ? working.getSubObject().getId() : null)
                .projectId(working.getSubObject() != null ? working.getSubObject().getProject().getId() : null)
                .build();
    }

    public Working toEntity(WorkingRequestDto workingDto) {
        return Working.builder()
                .name(workingDto.getName())
                .units(workingDto.getUnits())
                .quantity(workingDto.getQuantity())
                .done(workingDto.getDone())
                .unitPrice(workingDto.getUnitPrice())
                .standard(standardRepository.findById(workingDto.getStandardId()).orElse(null))
                .subObject(subObjectRepository.findById(workingDto.getSubObjectId()).orElse(null))
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
