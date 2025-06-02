package com.executive_documentation.acts.dto;

import com.executive_documentation.acts.model.EntranceControl;
import org.springframework.stereotype.Component;

@Component
public class EntranceControlMapper {

    public EntranceControlResponseDto toResponseDto(EntranceControl entranceControl) {
        if (entranceControl == null) {
            return null;
        }

        return EntranceControlResponseDto.builder()
                .id(entranceControl.getId())
                .controlNumber(entranceControl.getControlNumber())
                .date(entranceControl.getDate())
                .materials(entranceControl.getMaterials())
                .documents(entranceControl.getDocuments())
                .author(entranceControl.getAuthor())
                .standard(entranceControl.getStandard())
                .controlSheetNumbers(entranceControl.getControlSheetNumbers())
                .build();
    }
}
