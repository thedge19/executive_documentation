package com.executive_documentation.acts.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Builder
@Data
public class ActRequestDto {
    private Long projectId;
    private Long subObjectId;
    private Long workId;
    private Long nextWorkId;
    private BigDecimal workDone;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate controlDate;
    private String executiveSchema;
    private List<MaterialQuantityDto> materials;
}
