package com.executive_documentation.registries.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class RegistryPeriodDto {
    private int monthId;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    // getters/setters
}
