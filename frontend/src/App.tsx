import { useState, type FormEvent } from 'react'
import './App.css'
import { AmortizationChart } from './components/AmortizationChart'
import { MortgageForm } from './components/MortgageForm'
import { PriceComparison } from './components/PriceComparison'
import { calculateScenario, formatCurrency } from './lib/mortgage'
import { calculateAffordability } from './services/mortgageApi'
import type { AffordabilityRequest, AffordabilityResponse, MortgageFormValues } from './types/mortgage'

const initialValues: MortgageFormValues = {
  annualSalary: '65000',
  depositAmount: '45000',
  monthlyDebt: '250',
  targetPostcodeArea: 'CF',
  propertyType: '',
  mortgageTermYears: '25',
  annualInterestRate: '4',
}

function App() {
  const [values, setValues] = useState(initialValues)
  const [result, setResult] = useState<AffordabilityResponse | null>(null)
  const [requestedValues, setRequestedValues] = useState<MortgageFormValues | null>(null)
  const [isLoading, setIsLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const scenario = calculateScenario(values)

  const marketIsCurrent = result !== null
    && requestedValues?.targetPostcodeArea === values.targetPostcodeArea
    && requestedValues?.propertyType === values.propertyType
  const scheduleIsCurrent = marketIsCurrent
    && requestedValues?.annualSalary === values.annualSalary
    && requestedValues?.depositAmount === values.depositAmount
    && requestedValues?.monthlyDebt === values.monthlyDebt
    && requestedValues?.mortgageTermYears === values.mortgageTermYears
    && requestedValues?.annualInterestRate === values.annualInterestRate

  function updateValue<Key extends keyof MortgageFormValues>(
    key: Key,
    value: MortgageFormValues[Key],
  ) {
    setValues((current) => ({ ...current, [key]: value }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setIsLoading(true)
    setError(null)
    setResult(null)
    setRequestedValues(null)

    const request: AffordabilityRequest = {
      annualSalary: Number(values.annualSalary),
      depositAmount: Number(values.depositAmount),
      monthlyDebt: Number(values.monthlyDebt),
      targetPostcodeArea: values.targetPostcodeArea,
      propertyType: values.propertyType || undefined,
      mortgageTermYears: Number(values.mortgageTermYears),
      annualInterestRate: Number(values.annualInterestRate),
    }

    try {
      const response = await calculateAffordability(request)
      setResult(response)
      setRequestedValues({ ...values })
    } catch (requestError) {
      setError(requestError instanceof Error
        ? requestError.message
        : 'We could not complete your affordability check. Please try again.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <div className="app-shell">
      <header className="topbar">
        <a className="brand" href="#" aria-label="Cymru home">
          <span className="brand-mark" aria-hidden="true">
            <svg viewBox="0 0 32 32" fill="none">
              <path d="M5 14.2 16 5l11 9.2M8 13v13h16V13M13 26v-8h6v8" />
              <path d="M4 14.5 16 4l12 10.5" />
            </svg>
          </span>
          <span className="brand-name">cymru<span>.</span></span>
        </a>
        <div className="topbar-meta">
          <span className="live-dot" aria-hidden="true" />
          <span>Welsh property insights</span>
          <span className="topbar-divider" />
          <span className="topbar-region">UNITED KINGDOM</span>
        </div>
      </header>

      <main>
        <section className="intro">
          <div>
            <p className="eyebrow"><span>YOUR HOME, IN FOCUS</span><span className="eyebrow-line" /></p>
            <h1>A clearer view of<br className="desktop-break" /> your next move.</h1>
            <p className="intro-copy">
              See what feels possible, then compare it with the real picture across Wales.
            </p>
          </div>
          <div className="intro-note">
            <span className="note-icon" aria-hidden="true">✳</span>
            <p><strong>Built around real life.</strong><br />A starting point for your home-buying journey, not financial advice.</p>
          </div>
        </section>

        <div className="dashboard-grid">
          <aside className="form-column">
            <MortgageForm
              values={values}
              isLoading={isLoading}
              onChange={updateValue}
              onSubmit={handleSubmit}
            />
            <div className="assumption-note">
              <span className="assumption-icon" aria-hidden="true">i</span>
              <p>Estimate uses a 4.5× income multiple. Lenders may apply different criteria.</p>
            </div>
          </aside>

          <section className="results-column" aria-label="Affordability results">
            {error && (
              <div className="error-banner" role="alert">
                <span className="error-symbol" aria-hidden="true">!</span>
                <div>
                  <strong>We couldn’t load the Welsh market data.</strong>
                  <p>{error}</p>
                  <span>Your personal estimate is still shown below.</span>
                </div>
              </div>
            )}

            <div className="metrics-grid">
              <article className="metric-card metric-primary">
                <div className="metric-heading">
                  <span className="metric-icon metric-icon-light" aria-hidden="true">⌂</span>
                  <span>YOUR MAXIMUM BUDGET</span>
                </div>
                <p className="metric-value">{formatCurrency(scenario.maxPurchasePrice)}</p>
                <p className="metric-caption">Based on your income and deposit</p>
                <div className="metric-footer">
                  <span>Maximum loan</span>
                  <strong>{formatCurrency(scenario.maxLoanAmount)}</strong>
                </div>
              </article>

              <article className="metric-card">
                <div className="metric-heading">
                  <span className="metric-icon" aria-hidden="true">↗</span>
                  <span>ESTIMATED MONTHLY</span>
                </div>
                <p className="metric-value">{formatCurrency(scenario.monthlyRepayment)}</p>
                <p className="metric-caption">At {Number(values.annualInterestRate).toFixed(2)}% over {values.mortgageTermYears} years</p>
                <div className="metric-footer">
                  <span>Loan-to-value</span>
                  <strong>{scenario.loanToValue.toFixed(1)}%</strong>
                </div>
              </article>
            </div>

            <PriceComparison
              budget={scenario.maxPurchasePrice}
              areaPrice={marketIsCurrent ? Number(result?.averageAreaPrice ?? 0) : null}
              area={values.targetPostcodeArea}
              salesCount={marketIsCurrent ? result?.totalSalesInArea ?? 0 : null}
              isAffordable={marketIsCurrent ? result?.isAffordable ?? result?.affordable ?? null : null}
              hasResult={marketIsCurrent}
            />

            <section className="schedule-card">
              <div className="section-heading">
                <div>
                  <p className="section-kicker">THE LONG VIEW</p>
                  <h2>Your mortgage, over time</h2>
                </div>
                {scheduleIsCurrent && result && (
                  <div className="schedule-totals">
                    <span className="schedule-total">
                      <span>Total repaid</span>
                      <strong>{formatCurrency(scenario.totalRepayment)}</strong>
                    </span>
                    <span className="schedule-total">
                      <span>Total interest</span>
                      <strong>{formatCurrency(scenario.totalInterest)}</strong>
                    </span>
                  </div>
                )}
              </div>
              {scheduleIsCurrent && result?.amortizationSchedule?.length ? (
                <AmortizationChart schedule={result.amortizationSchedule} />
              ) : (
                <div className="chart-empty">
                  <div className="empty-chart-mark" aria-hidden="true">
                    <span /><span /><span /><span /><span />
                  </div>
                  <p>Run your check to see how your mortgage balance could change over time.</p>
                </div>
              )}
            </section>

            {marketIsCurrent && result?.areaPriceBreakdown && Object.keys(result.areaPriceBreakdown).length > 0 && (
              <details className="property-breakdown">
                <summary>Average prices by property type <span aria-hidden="true">＋</span></summary>
                <div className="breakdown-list">
                  {Object.entries(result.areaPriceBreakdown).map(([type, price]) => (
                    <div className="breakdown-row" key={type}>
                      <span>{propertyTypeLabel(type)}</span><strong>{formatCurrency(Number(price))}</strong>
                    </div>
                  ))}
                </div>
              </details>
            )}
          </section>
        </div>
      </main>

      <footer className="page-footer">
        <span>CYMRU HOME CHECK <span className="footer-dot">·</span> MADE FOR THE NEXT CHAPTER</span>
        <span>Property prices are historical averages and are not a valuation.</span>
      </footer>
    </div>
  )
}

function propertyTypeLabel(type: string) {
  const labels: Record<string, string> = {
    D: 'Detached',
    S: 'Semi-detached',
    T: 'Terraced',
    F: 'Flats & maisonettes',
    O: 'Other',
  }
  return labels[type] ?? type
}

export default App
