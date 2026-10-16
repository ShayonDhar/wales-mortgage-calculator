import { describe, expect, it } from 'vitest'
import { calculateScenario, formatCurrency } from './mortgage'
import type { MortgageFormValues } from '../types/mortgage'

const defaultValues: MortgageFormValues = {
  annualSalary: '65000',
  depositAmount: '45000',
  monthlyDebt: '250',
  targetPostcodeArea: 'CF',
  propertyType: '',
  mortgageTermYears: '25',
  annualInterestRate: '4',
}

describe('calculateScenario', () => {
  it('applies the 4.5x income multiple after annualising monthly debt', () => {
    expect(calculateScenario(defaultValues)).toMatchObject({
      maxLoanAmount: 279000,
      maxPurchasePrice: 324000,
      loanToValue: 86.11111111111111,
    })
  })

  it('calculates the repayment, total repayment, and interest for a positive rate', () => {
    const result = calculateScenario(defaultValues)

    expect(result.monthlyRepayment).toBeCloseTo(1472.66, 2)
    expect(result.totalRepayment).toBeCloseTo(441799.44, 2)
    expect(result.totalInterest).toBeCloseTo(result.totalRepayment - result.maxLoanAmount, 8)
  })

  it('uses straight-line repayment when the interest rate is zero', () => {
    const result = calculateScenario({ ...defaultValues, annualInterestRate: '0' })

    expect(result.monthlyRepayment).toBe(930)
    expect(result.totalInterest).toBe(0)
    expect(result.totalRepayment).toBe(279000)
  })

  it('does not produce negative borrowing or an invalid LTV', () => {
    const result = calculateScenario({
      ...defaultValues,
      annualSalary: '1000',
      monthlyDebt: '500',
      depositAmount: '0',
    })

    expect(result.maxLoanAmount).toBe(0)
    expect(result.maxPurchasePrice).toBe(0)
    expect(result.monthlyRepayment).toBe(0)
    expect(result.loanToValue).toBe(0)
  })

  it('formats pounds in en-GB and safely handles non-finite display values', () => {
    expect(formatCurrency(123456.78)).toBe('£123,457')
    expect(formatCurrency(Number.NaN)).toBe('£0')
  })
})
