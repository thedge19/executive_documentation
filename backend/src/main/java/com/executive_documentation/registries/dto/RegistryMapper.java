package com.executive_documentation.registries.dto;

import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RegistryMapper {

    public RegistryPeriodDto requestDtoToPeriodDto(RegistryRequestDto dto) {
        if (dto == null) {
            return null;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.parse(dto.getStartDate(), formatter);
        LocalDate endDate = LocalDate.parse(dto.getEndDate(), formatter);
        int months = startDate.getMonth().getValue();
        int years = startDate.getYear();

        return RegistryPeriodDto.builder()
                .monthId(months)
                .year(years)
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }



    private static String formatLocalDate(@NotNull LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
