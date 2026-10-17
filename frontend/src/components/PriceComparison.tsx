import { formatCurrency } from '../lib/mortgage'

interface PriceComparisonProps {
  budget: number
  areaPrice: number | null
  area: string
  salesCount: number | null
  isAffordable: boolean | null
  hasResult: boolean
}

export function PriceComparison({
  budget,
  areaPrice,
  area,
  salesCount,
  isAffordable,
  hasResult,
}: PriceComparisonProps) {
  const hasMarketPrice = areaPrice !== null && areaPrice > 0
  const highestValue = Math.max(budget, hasMarketPrice ? areaPrice : 0, 1)
  const budgetHeight = Math.max(4, budget / highestValue * 100)
  const areaHeight = hasMarketPrice ? Math.max(4, areaPrice / highestValue * 100) : 4

  return (
    <section className="comparison-card" aria-labelledby="comparison-title">
      <div className="section-heading comparison-heading">
        <div>
          <p className="section-kicker">THE REALITY CHECK</p>
          <h2 id="comparison-title">Your budget, meet the market.</h2>
        </div>
        {hasMarketPrice && (
          <span className={`affordability-tag ${isAffordable ? 'is-affordable' : 'is-stretch'}`}>
            <span aria-hidden="true">{isAffordable ? '✓' : '↗'}</span>
            {isAffordable ? 'Within reach' : 'A stretch'}
          </span>
        )}
      </div>

      <div className="comparison-chart" role="img" aria-label={
        hasMarketPrice
          ? `Maximum budget ${formatCurrency(budget)} compared with average ${formatCurrency(areaPrice)} in ${area}`
          : `Maximum budget ${formatCurrency(budget)}. Run a check to load the average property price for ${area}.`
      }>
        <div className="chart-grid-lines" aria-hidden="true">
          <span /><span /><span />
        </div>
        <div className="bar-pair">
          <div className="bar-column">
            <strong className="bar-value">{formatCurrency(budget)}</strong>
            <div className="bar-track">
              <div className="comparison-bar budget-bar" style={{ height: `${budgetHeight}%` }} />
            </div>
            <span className="bar-label"><i className="legend-dot budget-dot" />Your max budget</span>
          </div>
          <div className="bar-column">
            <strong className={`bar-value ${!hasMarketPrice ? 'bar-value-muted' : ''}`}>
              {hasMarketPrice ? formatCurrency(areaPrice) : '—'}
            </strong>
            <div className="bar-track">
              <div className="comparison-bar market-bar" style={{ height: `${areaHeight}%` }} />
            </div>
            <span className="bar-label"><i className="legend-dot market-dot" />Average {area} home</span>
          </div>
        </div>
      </div>

      <div className="comparison-footer">
        <p>
          {hasMarketPrice
            ? `${salesCount?.toLocaleString('en-GB') ?? '—'} recorded sales inform this area average.`
            : hasResult
              ? 'No transaction data is available for this selection yet.'
              : 'Run your check to compare your budget with local property prices.'}
        </p>
        <span>HM LAND REGISTRY DATA</span>
      </div>
    </section>
  )
}
