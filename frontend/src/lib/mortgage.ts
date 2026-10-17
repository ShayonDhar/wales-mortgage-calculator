import type { MortgageFormValues } from '../types/mortgage'

const incomeMultiple = 4.5
const monthsInYear = 12

export function calculateScenario(values: MortgageFormValues) {
  const annualSalary = Math.max(0, Number(values.annualSalary) || 0)
  const monthlyDebt = Math.max(0, Number(values.monthlyDebt) || 0)
  const deposit = Math.max(0, Number(values.depositAmount) || 0)
  const termYears = Math.max(1, Number(values.mortgageTermYears) || 1)
  const annualRate = Math.max(0, Number(values.annualInterestRate) || 0)
  const usableIncome = Math.max(0, annualSalary - monthlyDebt * monthsInYear)
  const maxLoanAmount = usableIncome * incomeMultiple
  const maxPurchasePrice = maxLoanAmount + deposit
  const monthlyRate = annualRate / 100 / monthsInYear
  const paymentCount = termYears * monthsInYear
  const monthlyRepayment = maxLoanAmount === 0
    ? 0
    : monthlyRate === 0
      ? maxLoanAmount / paymentCount
      : maxLoanAmount * monthlyRate * ((1 + monthlyRate) ** paymentCount)
        / (((1 + monthlyRate) ** paymentCount) - 1)

  return {
    maxLoanAmount,
    maxPurchasePrice,
    monthlyRepayment,
    totalRepayment: monthlyRepayment * paymentCount,
    totalInterest: Math.max(0, monthlyRepayment * paymentCount - maxLoanAmount),
    loanToValue: maxPurchasePrice > 0 ? maxLoanAmount / maxPurchasePrice * 100 : 0,
  }
}

export function formatCurrency(value: number) {
  return new Intl.NumberFormat('en-GB', {
    style: 'currency',
    currency: 'GBP',
    maximumFractionDigits: 0,
  }).format(Number.isFinite(value) ? value : 0)
}
