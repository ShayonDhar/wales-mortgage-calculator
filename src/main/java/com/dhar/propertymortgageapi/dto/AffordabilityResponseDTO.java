package com.dhar.propertymortgageapi.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class AffordabilityResponseDTO {
    private BigDecimal maxLoanAmount;
    private BigDecimal maxPurchasePrice;
    private BigDecimal averageAreaPrice;
    private BigDecimal estimatedMonthlyRepayment;
    private boolean isAffordable;
    private long totalSalesInArea;
}
