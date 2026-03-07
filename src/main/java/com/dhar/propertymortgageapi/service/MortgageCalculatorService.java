package com.dhar.propertymortgageapi.service;

import com.dhar.propertymortgageapi.dto.AffordabilityRequestDTO;
import com.dhar.propertymortgageapi.dto.AffordabilityResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
public class MortgageCalculatorService {

    // Loan-to-income limit multiplier
    private static final BigDecimal LTI_MULTIPLIER = new BigDecimal("4.5");
    private static final int MONTHS_IN_YEAR = 12;
    private static final BigDecimal MONTHS_IN_YEAR_BD = new BigDecimal("12");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

    // Scale for the final currency output (e.g., £0.00)
    private static final int FINAL_SCALE = 2;
    // High-precision scale for intermediate math to prevent rounding errors
    private static final int CALC_SCALE = 10;

    private final PropertyDataService propertyDataService;

    public AffordabilityResponseDTO calculateAffordability(AffordabilityRequestDTO request) {

        // Calculate Max Loan & Max Price
        BigDecimal annualizedDebt = request.getMonthlyDebt().multiply(MONTHS_IN_YEAR_BD);
        BigDecimal usableIncome = request.getAnnualSalary().subtract(annualizedDebt);

        // If debt is higher than income, loan is 0
        BigDecimal maxLoan = usableIncome.compareTo(BigDecimal.ZERO) > 0
                ? usableIncome.multiply(LTI_MULTIPLIER)
                : BigDecimal.ZERO;

        BigDecimal maxPurchasePrice = maxLoan.add(request.getDepositAmount());

        // Fetch Real-World Data from Database
        BigDecimal averageAreaPrice = propertyDataService.getAveragePrice(
                request.getTargetPostcodeArea().toUpperCase(),
                request.getPropertyType()
        );
        long salesCount = propertyDataService.getTransactionCount(request.getTargetPostcodeArea().toUpperCase());

        // Calculate Monthly Repayment (Amortization)
        BigDecimal monthlyRepayment = calculateMonthlyRepayment(
                maxLoan,
                request.getAnnualInterestRate(),
                request.getMortgageTermYears()
        );

        // Build and return the Response
        return AffordabilityResponseDTO.builder()
                .maxLoanAmount(maxLoan.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .maxPurchasePrice(maxPurchasePrice.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .averageAreaPrice(averageAreaPrice.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .estimatedMonthlyRepayment(monthlyRepayment.setScale(FINAL_SCALE, RoundingMode.HALF_UP))
                .isAffordable(maxPurchasePrice.compareTo(averageAreaPrice) >= 0)
                .totalSalesInArea(salesCount)
                .build();
    }

    private BigDecimal calculateMonthlyRepayment(BigDecimal principal, BigDecimal annualRate, int years) {
        if (principal.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        if (annualRate.compareTo(BigDecimal.ZERO) == 0) {
            // No Interest: Just divide principal by total months
            return principal.divide(new BigDecimal(years * MONTHS_IN_YEAR), FINAL_SCALE, RoundingMode.HALF_UP);
        }

        // r = Monthly interest rate (Annual Rate / 100 / 12)
        BigDecimal r = annualRate.divide(ONE_HUNDRED, CALC_SCALE, RoundingMode.HALF_UP)
                .divide(MONTHS_IN_YEAR_BD, CALC_SCALE, RoundingMode.HALF_UP);

        // n = Total number of payments
        int n = years * MONTHS_IN_YEAR;

        // Math: (1 + r)^n
        BigDecimal onePlusRToTheN = BigDecimal.ONE.add(r).pow(n);

        // Math: P * [ r * (1 + r)^n ] / [ (1 + r)^n - 1 ]
        BigDecimal numerator = principal.multiply(r).multiply(onePlusRToTheN);
        BigDecimal denominator = onePlusRToTheN.subtract(BigDecimal.ONE);

        return numerator.divide(denominator, FINAL_SCALE, RoundingMode.HALF_UP);
    }
}