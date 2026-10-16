import { fireEvent, render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import App from './App'
import type { AffordabilityResponse } from './types/mortgage'

const response: AffordabilityResponse = {
  maxLoanAmount: 279000,
  maxPurchasePrice: 324000,
  averageAreaPrice: 300000,
  estimatedMonthlyRepayment: 1473.22,
  isAffordable: true,
  totalSalesInArea: 1234,
  loanToValueRatio: 86.11,
  areaPriceBreakdown: { D: 420000, S: 300000, T: 250000, F: 190000 },
  amortizationSchedule: [
    { year: 1, interestPaid: 10980, principalPaid: 6688, remainingBalance: 272312 },
    { year: 5, interestPaid: 9430, principalPaid: 8238, remainingBalance: 230000 },
    { year: 25, interestPaid: 890, principalPaid: 16800, remainingBalance: 0 },
  ],
}

function mockSuccess(body: AffordabilityResponse = response) {
  const fetchMock = vi.fn().mockResolvedValue({
    ok: true,
    json: vi.fn().mockResolvedValue(body),
  })
  vi.stubGlobal('fetch', fetchMock)
  return fetchMock
}

describe('mortgage dashboard', () => {
  beforeEach(() => {
    vi.unstubAllGlobals()
  })

  it('shows an initial personal estimate and updates it as the interest rate changes', () => {
    render(<App />)

    expect(screen.getByText('£324,000', { selector: '.metric-value' })).toBeInTheDocument()
    expect(screen.getByText('£1,473')).toBeInTheDocument()

    fireEvent.change(screen.getByRole('slider', { name: 'Adjust annual interest rate' }), {
      target: { value: '5' },
    })

    expect(screen.getByText('£1,631')).toBeInTheDocument()
    expect(screen.getByText('At 5.00% over 25 years')).toBeInTheDocument()
  })

  it('submits the selected inputs and displays market and amortization results', async () => {
    const user = userEvent.setup()
    const fetchMock = mockSuccess()
    render(<App />)

    await user.selectOptions(screen.getByLabelText('Where are you looking?'), 'SA')
    await user.selectOptions(screen.getByLabelText('Property type'), 'T')
    await user.click(screen.getByRole('button', { name: 'See my reality check' }))

    await screen.findByText('Within reach')
    expect(fetchMock).toHaveBeenCalledWith(
      'http://localhost:8080/api/v1/mortgages/calculate',
      expect.objectContaining({
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          annualSalary: 65000,
          depositAmount: 45000,
          monthlyDebt: 250,
          targetPostcodeArea: 'SA',
          propertyType: 'T',
          mortgageTermYears: 25,
          annualInterestRate: 4,
        }),
      }),
    )
    expect(screen.getByText('1,234 recorded sales inform this area average.')).toBeInTheDocument()
    expect(screen.getByText('Total interest')).toBeInTheDocument()
    expect(screen.getByText('View yearly repayment breakdown')).toBeInTheDocument()

    await user.click(screen.getByText('View yearly repayment breakdown'))
    expect(screen.getByRole('table')).toBeInTheDocument()
    expect(within(screen.getByRole('table')).getByText('£230,000')).toBeInTheDocument()
  })

  it('clears market and schedule results while a new request is pending', async () => {
    const user = userEvent.setup()
    let finishRequest: ((value: { ok: boolean; json: () => Promise<AffordabilityResponse> }) => void) | undefined
    const fetchMock = vi.fn()
      .mockResolvedValueOnce({ ok: true, json: vi.fn().mockResolvedValue(response) })
      .mockImplementationOnce(() => new Promise((resolve) => { finishRequest = resolve }))
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'See my reality check' }))
    await screen.findByText('Within reach')
    await user.selectOptions(screen.getByLabelText('Where are you looking?'), 'NP')
    await user.click(screen.getByRole('button', { name: 'See my reality check' }))

    expect(screen.queryByText('Within reach')).not.toBeInTheDocument()
    expect(screen.getByText(/Run your check to compare your budget/)).toBeInTheDocument()
    expect(screen.getByRole('button', { name: /Checking your numbers/ })).toBeDisabled()
    expect(finishRequest).toBeDefined()
    finishRequest?.({ ok: true, json: vi.fn().mockResolvedValue({ ...response, averageAreaPrice: 250000 }) })

    await waitFor(() => expect(screen.getByText('Within reach')).toBeInTheDocument())
  })

  it('shows API errors but keeps the personal estimate available', async () => {
    const user = userEvent.setup()
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: false,
      status: 503,
      text: vi.fn().mockResolvedValue('Service temporarily unavailable'),
    }))
    render(<App />)

    await user.click(screen.getByRole('button', { name: 'See my reality check' }))

    const alert = await screen.findByRole('alert')
    expect(alert).toHaveTextContent('Service temporarily unavailable')
    expect(screen.getByText('£324,000', { selector: '.metric-value' })).toBeInTheDocument()
    expect(screen.getByText('Run your check to compare your budget with local property prices.')).toBeInTheDocument()
  })
})
