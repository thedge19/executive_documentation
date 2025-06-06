package com.executive_documentation.registries.dto;

import com.executive_documentation.registries.model.Registry;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Component
public class RegistryMapper {

    public RegistryResponseDto registryToRegistryResponseDto(Registry registry) {
        if (registry == null) {
            return null;
        }

        return RegistryResponseDto.builder()
                .id(registry.getId())
                .rowNumber(registry.getRowNumber())
                .monthId(registry.getMonthId())
                .documentName(registry.getDocumentName())
                .documentDate(formatLocalDate(registry.getDocumentDate()))
                .documentNumber(registry.getDocumentNumber())
                .documentAuthor(registry.getDocumentAuthor())
                .numberOfSheets(registry.getNumberOfSheets())
                .listInOrder(registry.getListInOrder())
                .build();
    }

    private static String formatLocalDate(@NotNull LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
    }
}
