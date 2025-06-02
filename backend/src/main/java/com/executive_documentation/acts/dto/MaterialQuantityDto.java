package com.executive_documentation.acts.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MaterialQuantityDto {
    private Long materialId;
    private Double quantity;
}
