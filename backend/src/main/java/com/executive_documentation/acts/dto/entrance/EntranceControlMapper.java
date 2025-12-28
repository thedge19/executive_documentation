package com.executive_documentation.acts.dto.entrance;

import com.executive_documentation.acts.model.EntranceControl;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class EntranceControlMapper {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    public EntranceControlResponseDto toResponseDto(EntranceControl entranceControl) {
        if (entranceControl == null) {
            return null;
        }

        return EntranceControlResponseDto.builder()
                .id(entranceControl.getId())
                .controlNumber(entranceControl.getControlNumber())
                .date(entranceControl.getDate()) // оставляем как LocalDate
                .build();
    }

    public EntranceControlExportDto toExportDto(EntranceControl entranceControl) {
        if (entranceControl == null) {
            return null;
        }

        return EntranceControlExportDto.builder()
                .id(entranceControl.getId())
                .date(formatDate(entranceControl.getDate()))
                .build();
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }
}
