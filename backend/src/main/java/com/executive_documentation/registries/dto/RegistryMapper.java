package com.executive_documentation.registries.dto;

import com.executive_documentation.registries.model.Registry;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Component
public class RegistryMapper {

    public RegistryResponseDto registryToRegistryResponseDto(Registry registry) {
        if (registry == null) {
            return null;
        }

        return RegistryResponseDto.builder()
                .id(registry.getId())
                .rowNumber(registry.getRowNumber())
                .documentName(registry.getDocumentName())
                .documentDate(formatLocalDate(registry.getDocumentDate()))
                .documentNumber(registry.getDocumentNumber())
                .documentAuthor(registry.getDocumentAuthor())
                .numberOfSheets(registry.getNumberOfSheets())
                .listInOrder(registry.getListInOrder())
                .build();
    }

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
