package com.executive_documentation.workings.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class WorkingExcelImportDto {
    private Long subObjectId;
    private String name;
    private String units;
    private BigDecimal quantity;
}
