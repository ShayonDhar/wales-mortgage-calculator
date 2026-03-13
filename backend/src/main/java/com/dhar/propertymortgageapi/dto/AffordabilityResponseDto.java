package com.dhar.propertymortgageapi.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for affordability calculation response.
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
    private BigDecimal loanToValueRatio;
    private Map<String, BigDecimal> areaPriceBreakdown; // Bar Chart Feature
    private List<AmortizationYearDto> amortizationSchedule; // Line Chart Feature
}