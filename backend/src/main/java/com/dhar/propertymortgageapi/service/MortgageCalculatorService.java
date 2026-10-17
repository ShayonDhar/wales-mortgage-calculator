package com.dhar.propertymortgageapi.service;

import com.dhar.propertymortgageapi.dto.AffordabilityRequestDto;
import com.dhar.propertymortgageapi.dto.AffordabilityResponseDto;
import com.dhar.propertymortgageapi.dto.AmortizationYearDto;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service for calculating mortgage affordability, repayments and amortization.
 */
@Service
@RequiredArgsConstructor
public class MortgageCalculatorService {

    private static final BigDecimal LTI_MULTIPLIER = new BigDecimal("4.5");
    private static final int MONTHS_IN_YEAR = 12;
    private static final BigDecimal MONTHS_IN_YEAR_BD = BigDecimal.valueOf(MONTHS_IN_YEAR);
    private static final BigDecimal ONE_HUNDRED = BigDecimal.valueOf(100);
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
        BigDecimal maxLoan = calculateMaxLoan(request.getAnnualSalary(), request.getMonthlyDebt());
        BigDecimal maxPurchasePrice = maxLoan.add(request.getDepositAmount());
        BigDecimal ltvRatio = calculateLtvRatio(maxLoan, maxPurchasePrice);

        String area = request.getTargetPostcodeArea().toUpperCase(Locale.ROOT);
        BigDecimal averageAreaPrice = propertyDataService.getAveragePrice(
                area, request.getPropertyType());
        long salesCount = propertyDataService.getTransactionCount(area);
        Map<String, BigDecimal> areaBreakdown = propertyDataService.getAreaPriceBreakdown(area);

        BigDecimal monthlyRate = toMonthlyRate(request.getAnnualInterestRate());
        int totalPayments = request.getMortgageTermYears() * MONTHS_IN_YEAR;
        BigDecimal monthlyRepayment = calculateMonthlyRepayment(
                maxLoan, monthlyRate, totalPayments);
        List<AmortizationYearDto> schedule = generateAmortizationSchedule(
                maxLoan, monthlyRate, request.getMortgageTermYears(), monthlyRepayment);

        return AffordabilityResponseDto.builder()
                .maxLoanAmount(roundCurrency(maxLoan))
                .maxPurchasePrice(roundCurrency(maxPurchasePrice))
                .averageAreaPrice(roundCurrency(averageAreaPrice))
                .estimatedMonthlyRepayment(roundCurrency(monthlyRepayment))
                .affordable(maxPurchasePrice.compareTo(averageAreaPrice) >= 0)
                .totalSalesInArea(salesCount)
                .loanToValueRatio(roundCurrency(ltvRatio))
                .areaPriceBreakdown(areaBreakdown)
                .amortizationSchedule(schedule)
                .build();
    }

    private BigDecimal calculateMaxLoan(BigDecimal annualSalary, BigDecimal monthlyDebt) {
        BigDecimal annualizedDebt = monthlyDebt.multiply(MONTHS_IN_YEAR_BD);
        BigDecimal usableIncome = annualSalary.subtract(annualizedDebt);

        return usableIncome.signum() > 0
                ? usableIncome.multiply(LTI_MULTIPLIER)
                : BigDecimal.ZERO;
    }

    private BigDecimal calculateLtvRatio(BigDecimal maxLoan, BigDecimal maxPurchasePrice) {
        if (maxPurchasePrice.signum() <= 0) {
            return BigDecimal.ZERO;
        }

        return maxLoan.divide(maxPurchasePrice, CALC_SCALE, RoundingMode.HALF_UP)
                .multiply(ONE_HUNDRED);
    }

    private BigDecimal toMonthlyRate(BigDecimal annualRatePercent) {
        if (annualRatePercent.signum() == 0) {
            return BigDecimal.ZERO;
        }

        return annualRatePercent.divide(ONE_HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR_BD, CALC_SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateMonthlyRepayment(
            BigDecimal principal, BigDecimal monthlyRate, int totalPayments) {
        if (principal.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        if (monthlyRate.signum() == 0) {
            return principal.divide(
                    BigDecimal.valueOf(totalPayments), FINAL_SCALE, RoundingMode.HALF_UP);
        }

        BigDecimal compoundingFactor = BigDecimal.ONE.add(monthlyRate).pow(totalPayments);
        BigDecimal numerator = principal.multiply(monthlyRate).multiply(compoundingFactor);
        BigDecimal denominator = compoundingFactor.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, FINAL_SCALE, RoundingMode.HALF_UP);
    }

    private List<AmortizationYearDto> generateAmortizationSchedule(
            BigDecimal principal, BigDecimal monthlyRate, int years, BigDecimal monthlyPayment) {
        List<AmortizationYearDto> schedule = new ArrayList<>();
        BigDecimal remainingBalance = principal;

        for (int year = 1; year <= years && remainingBalance.signum() > 0; year++) {
            BigDecimal yearlyInterestPaid = BigDecimal.ZERO;
            BigDecimal yearlyPrincipalPaid = BigDecimal.ZERO;

            for (int month = 1;
                    month <= MONTHS_IN_YEAR && remainingBalance.signum() > 0; month++) {
                BigDecimal interestForMonth = roundCurrency(remainingBalance.multiply(monthlyRate));

                // The rest of the payment goes to the principal; the final payment
                // clears any balance left over from rounding.
                boolean isFinalPayment = year == years && month == MONTHS_IN_YEAR;
                BigDecimal principalForMonth = isFinalPayment
                        ? remainingBalance
                        : monthlyPayment.subtract(interestForMonth).min(remainingBalance);

                remainingBalance = remainingBalance.subtract(principalForMonth);
                yearlyInterestPaid = yearlyInterestPaid.add(interestForMonth);
                yearlyPrincipalPaid = yearlyPrincipalPaid.add(principalForMonth);
            }

            schedule.add(AmortizationYearDto.builder()
                    .year(year)
                    .interestPaid(roundCurrency(yearlyInterestPaid))
                    .principalPaid(roundCurrency(yearlyPrincipalPaid))
                    .remainingBalance(roundCurrency(remainingBalance.max(BigDecimal.ZERO)))
                    .build());
        }

        return schedule;
    }

    private static BigDecimal roundCurrency(BigDecimal value) {
        return value.setScale(FINAL_SCALE, RoundingMode.HALF_UP);
    }
}
