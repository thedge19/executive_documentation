package com.executive_documentation.materials.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class MaterialQuantityDto {
    private Long materialId;
    private BigDecimal quantity;
}
