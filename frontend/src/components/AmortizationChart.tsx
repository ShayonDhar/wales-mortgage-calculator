import { formatCurrency } from '../lib/mortgage'
import type { AmortizationYear } from '../types/mortgage'

interface AmortizationChartProps {
  schedule: AmortizationYear[]
}

const chartWidth = 720
const chartHeight = 210
const leftPadding = 78
const rightPadding = 12
const topPadding = 14
const bottomPadding = 30

export function AmortizationChart({ schedule }: AmortizationChartProps) {
  const maxBalance = Math.max(...schedule.map((year) => year.remainingBalance), 1)
  const points = schedule.map((year, index) => {
    const x = leftPadding + (schedule.length <= 1
      ? 0
      : index / (schedule.length - 1) * (chartWidth - leftPadding - rightPadding))
    const y = topPadding + (1 - year.remainingBalance / maxBalance)
      * (chartHeight - topPadding - bottomPadding)
    return { ...year, x, y }
  })
  const linePoints = points.map((point) => `${point.x},${point.y}`).join(' ')
  const lastPoint = points[points.length - 1]
  const areaPoints = `${leftPadding},${chartHeight - bottomPadding} ${linePoints} ${lastPoint?.x ?? leftPadding},${chartHeight - bottomPadding}`
  const chartTicks = [1, 0.5, 0]

  return (
    <div className="amortization-chart">
      <div className="chart-legend"><span><i />Remaining loan balance</span><span>ESTIMATED BALANCE BY YEAR</span></div>
      <svg
        className="balance-svg"
        viewBox={`0 0 ${chartWidth} ${chartHeight}`}
        role="img"
        aria-label={`Mortgage balance decreases from ${formatCurrency(maxBalance)} in year one to ${formatCurrency(schedule[schedule.length - 1]?.remainingBalance ?? 0)} at the end of the term.`}
      >
        <defs>
          <linearGradient id="balance-fill" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stopColor="#60c1a0" stopOpacity=".23" />
            <stop offset="100%" stopColor="#60c1a0" stopOpacity=".015" />
          </linearGradient>
        </defs>
        {chartTicks.map((tick) => {
          const y = topPadding + (1 - tick) * (chartHeight - topPadding - bottomPadding)
          return (
            <g key={tick}>
              <line x1={leftPadding} y1={y} x2={chartWidth - rightPadding} y2={y} className="chart-gridline" />
              <text x={leftPadding - 10} y={y + 4} className="chart-axis-label" textAnchor="end">
                {formatCurrency(maxBalance * tick)}
              </text>
            </g>
          )
        })}
        <polygon points={areaPoints} fill="url(#balance-fill)" />
        <polyline points={linePoints} className="balance-line" />
        {points.map((point, index) => (
          <g key={point.year}>
            {(index === 0 || index === points.length - 1 || point.year % 5 === 0) && (
              <>
                <circle cx={point.x} cy={point.y} r="4" className="balance-point" />
                <text x={point.x} y={chartHeight - 8} className="chart-axis-label" textAnchor="middle">
                  Yr {point.year}
                </text>
              </>
            )}
            <title>{`Year ${point.year}: ${formatCurrency(point.remainingBalance)} remaining`}</title>
          </g>
        ))}
      </svg>
      <details className="schedule-details">
        <summary>View yearly repayment breakdown <span aria-hidden="true">＋</span></summary>
        <div className="schedule-table-wrap">
          <table>
            <thead><tr><th>Year</th><th>Principal paid</th><th>Interest paid</th><th>Balance remaining</th></tr></thead>
            <tbody>
              {schedule.map((year) => (
                <tr key={year.year}>
                  <td>{year.year}</td>
                  <td>{formatCurrency(year.principalPaid)}</td>
                  <td>{formatCurrency(year.interestPaid)}</td>
                  <td>{formatCurrency(year.remainingBalance)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </details>
    </div>
  )
}
