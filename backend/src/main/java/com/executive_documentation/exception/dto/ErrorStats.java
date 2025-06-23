package com.executive_documentation.exception.dto;

import com.executive_documentation.exception.model.ErrorLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorStats {
    private long totalErrors;
    private long last24Hours;
    private long last7Days;
    private Map<ErrorLevel, Long> countByLevel;
    private Map<LocalDate, Long> countByDay;
    private String mostCommonErrorMessage;
    private String mostFrequentEndpoint;
}
