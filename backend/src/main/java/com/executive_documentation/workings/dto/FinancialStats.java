package com.executive_documentation.workings.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FinancialStats {
    private BigDecimal totalAmount;
    private BigDecimal doneAmount;
}
