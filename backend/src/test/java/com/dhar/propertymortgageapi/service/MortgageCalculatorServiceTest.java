package com.dhar.propertymortgageapi.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dhar.propertymortgageapi.dto.AffordabilityRequestDto;
import com.dhar.propertymortgageapi.dto.AffordabilityResponseDto;
import com.dhar.propertymortgageapi.dto.AmortizationYearDto;
import java.math.BigDecimal;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MortgageCalculatorServiceTest {

    @Mock
    private PropertyDataService propertyDataService;

    @InjectMocks
    private MortgageCalculatorService mortgageCalculatorService;

    @Test
    void calculatesAffordabilityAndReturnsAreaDataAndAmortization() {
        when(propertyDataService.getAveragePrice("CF", "T")).thenReturn(amount("300000"));
        when(propertyDataService.getTransactionCount("CF")).thenReturn(125L);
        when(propertyDataService.getAreaPriceBreakdown("CF"))
                .thenReturn(Map.of("T", amount("250000")));

        AffordabilityResponseDto response =
                mortgageCalculatorService.calculateAffordability(request(
                        "65000", "45000", "250", "cf", "T", 25, "4"));

        assertAmount("279000.00", response.getMaxLoanAmount());
        assertAmount("324000.00", response.getMaxPurchasePrice());
        assertAmount("1472.66", response.getEstimatedMonthlyRepayment());
        assertAmount("86.11", response.getLoanToValueRatio());
        assertTrue(response.isAffordable());
        assertEquals(125L, response.getTotalSalesInArea());
        assertEquals(Map.of("T", amount("250000")), response.getAreaPriceBreakdown());
        assertEquals(25, response.getAmortizationSchedule().size());
        assertEquals(1, response.getAmortizationSchedule().get(0).getYear());
        assertEquals(25, response.getAmortizationSchedule().get(24).getYear());
        assertAmount("0.00", response.getAmortizationSchedule().get(24).getRemainingBalance());
        BigDecimal principalRepaid = response.getAmortizationSchedule().stream()
                .map(AmortizationYearDto::getPrincipalPaid)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assertAmount("279000.00", principalRepaid);
        verify(propertyDataService).getAveragePrice("CF", "T");
        verify(propertyDataService).getTransactionCount("CF");
        verify(propertyDataService).getAreaPriceBreakdown("CF");
    }

    @Test
    void marksPurchaseUnaffordableWhenBudgetIsBelowAreaAverage() {
        when(propertyDataService.getAveragePrice("SA", null)).thenReturn(amount("400000"));
        when(propertyDataService.getTransactionCount("SA")).thenReturn(0L);
        when(propertyDataService.getAreaPriceBreakdown("SA")).thenReturn(Map.of());

        AffordabilityResponseDto response =
                mortgageCalculatorService.calculateAffordability(request(
                        "40000", "10000", "0", "SA", null, 20, "4.5"));

        assertAmount("180000.00", response.getMaxLoanAmount());
        assertAmount("190000.00", response.getMaxPurchasePrice());
        assertFalse(response.isAffordable());
    }

    @Test
    void handlesZeroInterestWithStraightLineRepayments() {
        when(propertyDataService.getAveragePrice("NP", null)).thenReturn(amount("100000"));
        when(propertyDataService.getTransactionCount("NP")).thenReturn(1L);
        when(propertyDataService.getAreaPriceBreakdown("NP")).thenReturn(Map.of());

        AffordabilityResponseDto response =
                mortgageCalculatorService.calculateAffordability(request(
                        "30000", "0", "0", "NP", null, 10, "0"));

        assertAmount("135000.00", response.getMaxLoanAmount());
        assertAmount("1125.00", response.getEstimatedMonthlyRepayment());
        assertEquals(10, response.getAmortizationSchedule().size());
        AmortizationYearDto firstYear = response.getAmortizationSchedule().get(0);
        assertAmount("0.00", firstYear.getInterestPaid());
        assertAmount("13500.00", firstYear.getPrincipalPaid());
        assertAmount("121500.00", firstYear.getRemainingBalance());
        assertAmount("0.00", response.getAmortizationSchedule().get(9).getRemainingBalance());
    }

    @Test
    void capsLoanAtZeroWhenAnnualizedDebtExceedsIncome() {
        when(propertyDataService.getAveragePrice("LL", null)).thenReturn(amount("20000"));
        when(propertyDataService.getTransactionCount("LL")).thenReturn(0L);
        when(propertyDataService.getAreaPriceBreakdown("LL")).thenReturn(Map.of());

        AffordabilityResponseDto response =
                mortgageCalculatorService.calculateAffordability(request(
                        "20000", "25000", "2000", "LL", null, 25, "4"));

        assertAmount("0.00", response.getMaxLoanAmount());
        assertAmount("25000.00", response.getMaxPurchasePrice());
        assertAmount("0.00", response.getEstimatedMonthlyRepayment());
        assertAmount("0.00", response.getLoanToValueRatio());
        assertTrue(response.isAffordable());
        assertTrue(response.getAmortizationSchedule().isEmpty());
    }

    private static AffordabilityRequestDto request(
            String salary,
            String deposit,
            String monthlyDebt,
            String postcodeArea,
            String propertyType,
            int termYears,
            String annualRate) {
        AffordabilityRequestDto request = new AffordabilityRequestDto();
        request.setAnnualSalary(amount(salary));
        request.setDepositAmount(amount(deposit));
        request.setMonthlyDebt(amount(monthlyDebt));
        request.setTargetPostcodeArea(postcodeArea);
        request.setPropertyType(propertyType);
        request.setMortgageTermYears(termYears);
        request.setAnnualInterestRate(amount(annualRate));
        return request;
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }

    private static void assertAmount(String expected, BigDecimal actual) {
        assertEquals(0, amount(expected).compareTo(actual), "Unexpected monetary value");
    }
}
