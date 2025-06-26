package com.executive_documentation.workings.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class TotalFinancialStats {
    private BigDecimal totalDone;
    private BigDecimal totalAmount;
}
