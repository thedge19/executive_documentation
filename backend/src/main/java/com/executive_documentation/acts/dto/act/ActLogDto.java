package com.executive_documentation.acts.dto.act;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
public class ActLogDto {
    private Long id;

    private String works;

    private LocalDate startDate;

    private LocalDate endDate;

    private BigDecimal workDone;
}
