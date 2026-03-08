package com.dhar.propertymortgageapi.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

/**
 * Data Transfer Object for amortization response data.
 */
@Data
@Builder
public class AmortizationYearDto {
    private int year;
    private BigDecimal interestPaid;
    private BigDecimal principalPaid;
    private BigDecimal remainingBalance;
}