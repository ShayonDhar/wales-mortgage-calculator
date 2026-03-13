package com.dhar.propertymortgageapi.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

/**
 * Data Transfer Object for affordability calculation requests.
 */
@Data
public class AffordabilityRequestDto {
    @NotNull(message = "Salary is required")
    @Min(value = 0, message = "Salary cannot be negative")
    private BigDecimal annualSalary;

    @NotNull
    @Min(0)
    private BigDecimal depositAmount;

    @NotNull
    @Min(0)
    private BigDecimal monthlyDebt;

    @NotBlank(message = "Postcode area is required")
    private String targetPostcodeArea;

    private String propertyType;

    @NotNull
    @Min(1)
    private Integer mortgageTermYears;

    @NotNull
    @Min(0)
    private BigDecimal annualInterestRate;
}