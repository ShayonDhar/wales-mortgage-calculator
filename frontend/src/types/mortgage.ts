export interface MortgageFormValues {
  annualSalary: string
  depositAmount: string
  monthlyDebt: string
  targetPostcodeArea: string
  propertyType: string
  mortgageTermYears: string
  annualInterestRate: string
}

export interface AffordabilityRequest {
  annualSalary: number
  depositAmount: number
  monthlyDebt: number
  targetPostcodeArea: string
  propertyType?: string
  mortgageTermYears: number
  annualInterestRate: number
}

export interface AmortizationYear {
  year: number
  interestPaid: number
  principalPaid: number
  remainingBalance: number
}

export interface AffordabilityResponse {
  maxLoanAmount: number
  maxPurchasePrice: number
  averageAreaPrice: number
  estimatedMonthlyRepayment: number
  affordable: boolean
  totalSalesInArea: number
  loanToValueRatio: number
  areaPriceBreakdown: Record<string, number>
  amortizationSchedule: AmortizationYear[]
}
