package com.executive_documentation.acts.dto.materials;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@RequiredArgsConstructor
@Data
public class MaterialDto {
    private Long materialId;
    private BigDecimal quantity;
}
