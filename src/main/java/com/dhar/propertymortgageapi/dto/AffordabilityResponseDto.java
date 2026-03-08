package com.dhar.propertymortgageapi.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for affordability calculation responses.
 */
@Data
@Builder
public class AffordabilityResponseDto {

    private BigDecimal maxLoanAmount;
    private BigDecimal maxPurchasePrice;
    private BigDecimal averageAreaPrice;
    private BigDecimal estimatedMonthlyRepayment;
    private boolean isAffordable;
    private long totalSalesInArea;
}