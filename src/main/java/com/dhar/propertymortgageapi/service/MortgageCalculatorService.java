package com.dhar.propertymortgageapi.service;

import com.dhar.propertymortgageapi.dto.AffordabilityRequestDto;
import com.dhar.propertymortgageapi.dto.AffordabilityResponseDto;
import com.dhar.propertymortgageapi.dto.AmortizationYearDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for processing/calculating mortgage data.
 */
@Service
@RequiredArgsConstructor
public class MortgageCalculatorService {

    private static final BigDecimal LTI_MULTIPLIER = new BigDecimal("4.5");
    private static final int MONTHS_IN_YEAR = 12;
    private static final BigDecimal MONTHS_IN_YEAR_BD = new BigDecimal("12");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final int FINAL_SCALE = 2;
    private static final int CALC_SCALE = 10;

    private final PropertyDataService propertyDataService;

    /**
     * Calculates mortgage affordability based on the provided request parameters.
     *
     * @param request the affordability request parameters
     * @return an AffordabilityResponseDto containing calculation results
     */
    public AffordabilityResponseDto calculateAffordability(AffordabilityRequestDto request) {

        // Calculate Core Max Loan & Price
        BigDecimal annualizedDebt = request.getMonthlyDebt().multiply(MONTHS_IN_YEAR_BD);
        BigDecimal usableIncome = request.getAnnualSalary().subtract(annualizedDebt);

        BigDecimal maxLoan = usableIncome.compareTo(BigDecimal.ZERO) > 0
                ? usableIncome.multiply(LTI_MULTIPLIER)
                : BigDecimal.ZERO;

        BigDecimal maxPurchasePrice = maxLoan.add(request.getDepositAmount());

        // Calculate LTV (Loan To Value Ratio)
        BigDecimal ltvRatio = BigDecimal.ZERO;
        if (maxPurchasePrice.compareTo(BigDecimal.ZERO) > 0) {
            ltvRatio = maxLoan.divide(maxPurchasePrice, CALC_SCALE, RoundingMode.HALF_UP)
                    .multiply(ONE_HUNDRED);
        }

        // Fetch Area Breakdown for Bar Charts
        String area = request.getTargetPostcodeArea().toUpperCase();
        BigDecimal averageAreaPrice = propertyDataService.getAveragePrice(
                area, request.getPropertyType());
        long salesCount = propertyDataService.getTransactionCount(area);
        Map<String, BigDecimal> areaBreakdown = propertyDataService.getAreaPriceBreakdown(area);

        // Calculate Monthly Repayment
        BigDecimal monthlyRepayment = calculateMonthlyRepayment(
                maxLoan, request.getAnnualInterestRate(), request.getMortgageTermYears());

        // Generate Amortization Schedule for Line Charts
        List<AmortizationYearDto> schedule = generateAmortizationSchedule(
                maxLoan, request.getAnnualInterestRate(),
                request.getMortgageTermYears(), monthlyRepayment);

        // Build final response
        return AffordabilityResponseDto.builder()
                .maxLoanAmount(maxLoan.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .maxPurchasePrice(maxPurchasePrice.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .averageAreaPrice(averageAreaPrice.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .estimatedMonthlyRepayment(
                        monthlyRepayment.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .isAffordable(maxPurchasePrice.compareTo(averageAreaPrice) >= 0)
                .totalSalesInArea(salesCount)
                .loanToValueRatio(ltvRatio.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .areaPriceBreakdown(areaBreakdown)
                .amortizationSchedule(schedule)
                .build();
    }

    private BigDecimal calculateMonthlyRepayment(
            BigDecimal principal, BigDecimal annualRate, int years) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            return principal.divide(
                    new BigDecimal(years * MONTHS_IN_YEAR), FINAL_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal monthlyRate = annualRate.divide(ONE_HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR_BD, CALC_SCALE, RoundingMode.HALF_UP);
        int totalPayments = years * MONTHS_IN_YEAR;

        BigDecimal compoundingFactor = BigDecimal.ONE.add(monthlyRate).pow(totalPayments);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(compoundingFactor);
        BigDecimal denominator = compoundingFactor.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, FINAL_SCALE, RoundingMode.HALF_UP);
    }

    private List<AmortizationYearDto> generateAmortizationSchedule(
            BigDecimal principal, BigDecimal annualRate, int years, BigDecimal monthlyPayment) {

        List<AmortizationYearDto> schedule = new ArrayList<>();
        BigDecimal remainingBalance = principal;

        BigDecimal monthlyRate = annualRate.compareTo(BigDecimal.ZERO) == 0
                ? BigDecimal.ZERO
                : annualRate.divide(ONE_HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR_BD, CALC_SCALE, RoundingMode.HALF_UP);

        for (int year = 1; year <= years; year++) {
            BigDecimal yearlyInterestPaid = BigDecimal.ZERO;
            BigDecimal yearlyPrincipalPaid = BigDecimal.ZERO;

            for (int month = 1; month <= 12; month++) {
                if (remainingBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    break;
                }

                // Interest for this specific month
                BigDecimal interestForMonth = remainingBalance.multiply(monthlyRate)
                        .setScale(FINAL_SCALE, RoundingMode.HALF_UP);
                // The rest of the payment goes to the principal
                BigDecimal principalForMonth = monthlyPayment.subtract(interestForMonth);

                remainingBalance = remainingBalance.subtract(principalForMonth);
                yearlyInterestPaid = yearlyInterestPaid.add(interestForMonth);
                yearlyPrincipalPaid = yearlyPrincipalPaid.add(principalForMonth);
            }

            schedule.add(AmortizationYearDto.builder()
                    .year(year)
                    .interestPaid(yearlyInterestPaid.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                    .principalPaid(yearlyPrincipalPaid.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                    .remainingBalance(
                            remainingBalance.max(BigDecimal.ZERO)
                                            .setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                    .build());
        }
        return schedule;
    }
}