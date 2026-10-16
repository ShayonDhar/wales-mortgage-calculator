import type { CSSProperties, FormEvent } from 'react'
import { formatCurrency } from '../lib/mortgage'
import { mortgageTermOptions, postcodeAreas, propertyTypes } from '../lib/options'
import type { MortgageFormValues } from '../types/mortgage'

interface MortgageFormProps {
  values: MortgageFormValues
  isLoading: boolean
  onChange: <Key extends keyof MortgageFormValues>(
    key: Key,
    value: MortgageFormValues[Key],
  ) => void
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}

const minDepositSliderMax = 150000
const depositSliderStep = 10000
const maxInterestRate = 8

export function MortgageForm({ values, isLoading, onChange, onSubmit }: MortgageFormProps) {
  const depositValue = Number(values.depositAmount) || 0
  const depositSliderMax = Math.max(
    minDepositSliderMax,
    Math.ceil(depositValue / depositSliderStep) * depositSliderStep,
  )
  const rateValue = Number(values.annualInterestRate) || 0

  return (
    <form className="form-card" onSubmit={onSubmit}>
      <div className="form-heading">
        <div>
          <p className="section-kicker">LET’S START WITH YOU</p>
          <h2>Your numbers</h2>
        </div>
        <span className="form-step">01 <span>/ 02</span></span>
      </div>

      <label className="field-label" htmlFor="annual-salary">Annual household income</label>
      <div className="input-wrap">
        <span className="input-prefix" aria-hidden="true">£</span>
        <input
          id="annual-salary"
          type="number"
          min="0"
          step="1000"
          inputMode="numeric"
          value={values.annualSalary}
          onChange={(event) => onChange('annualSalary', event.target.value)}
          required
        />
      </div>
      <p className="field-hint">Before tax, for everyone on the mortgage</p>

      <div className="field-row">
        <div className="field-group">
          <label className="field-label" htmlFor="deposit">Your deposit</label>
          <div className="input-wrap">
            <span className="input-prefix" aria-hidden="true">£</span>
            <input
              id="deposit"
              type="number"
              min="0"
              step="1000"
              inputMode="numeric"
              value={values.depositAmount}
              onChange={(event) => onChange('depositAmount', event.target.value)}
              required
            />
          </div>
        </div>
        <div className="field-group">
          <label className="field-label" htmlFor="monthly-debt">Monthly debt</label>
          <div className="input-wrap">
            <span className="input-prefix" aria-hidden="true">£</span>
            <input
              id="monthly-debt"
              type="number"
              min="0"
              step="50"
              inputMode="numeric"
              value={values.monthlyDebt}
              onChange={(event) => onChange('monthlyDebt', event.target.value)}
              required
            />
          </div>
        </div>
      </div>

      <div className="slider-field">
        <div className="slider-heading">
          <label className="field-label" htmlFor="deposit-slider">Explore your deposit</label>
          <span>{formatCurrency(depositValue)}</span>
        </div>
        <input
          className="range-input"
          id="deposit-slider"
          type="range"
          min="0"
          max={depositSliderMax}
          step="1000"
          value={Math.min(depositValue, depositSliderMax)}
          style={{ '--range-progress': `${(depositValue / depositSliderMax) * 100}%` } as CSSProperties}
          onChange={(event) => onChange('depositAmount', event.target.value)}
          aria-label="Adjust deposit amount"
        />
        <div className="range-labels"><span>£0</span><span>{formatCurrency(depositSliderMax)}</span></div>
      </div>

      <div className="field-group field-spaced">
        <label className="field-label" htmlFor="postcode-area">Where are you looking?</label>
        <div className="select-wrap">
          <select
            id="postcode-area"
            value={values.targetPostcodeArea}
            onChange={(event) => onChange('targetPostcodeArea', event.target.value)}
          >
            {postcodeAreas.map(({ code, name }) => <option key={code} value={code}>{name} · {code}</option>)}
          </select>
          <span className="select-arrow" aria-hidden="true">⌄</span>
        </div>
      </div>

      <div className="field-group field-spaced">
        <label className="field-label" htmlFor="property-type">Property type</label>
        <div className="select-wrap">
          <select
            id="property-type"
            value={values.propertyType}
            onChange={(event) => onChange('propertyType', event.target.value)}
          >
            <option value="">All property types</option>
            {propertyTypes.map(({ code, name }) => <option key={code} value={code}>{name}</option>)}
          </select>
          <span className="select-arrow" aria-hidden="true">⌄</span>
        </div>
      </div>

      <div className="field-row field-spaced">
        <div className="field-group">
          <label className="field-label" htmlFor="mortgage-term">Mortgage term</label>
          <div className="select-wrap">
            <select
              id="mortgage-term"
              value={values.mortgageTermYears}
              onChange={(event) => onChange('mortgageTermYears', event.target.value)}
            >
              {mortgageTermOptions.map((term) => (
                <option key={term} value={term}>{term} years</option>
              ))}
            </select>
            <span className="select-arrow" aria-hidden="true">⌄</span>
          </div>
        </div>
        <div className="field-group">
          <div className="slider-heading rate-label-heading">
            <label className="field-label" htmlFor="interest-rate">Interest rate</label>
            <span>{rateValue.toFixed(2)}%</span>
          </div>
          <input
            className="range-input rate-slider"
            id="interest-rate"
            type="range"
            min="0"
            max={maxInterestRate}
            step="0.05"
            value={rateValue}
            style={{ '--range-progress': `${(rateValue / maxInterestRate) * 100}%` } as CSSProperties}
            onChange={(event) => onChange('annualInterestRate', event.target.value)}
            aria-label="Adjust annual interest rate"
          />
          <div className="range-labels"><span>0%</span><span>{maxInterestRate}%</span></div>
        </div>
      </div>

      <button className="submit-button" type="submit" disabled={isLoading}>
        <span>{isLoading ? 'Checking your numbers…' : 'See my reality check'}</span>
        <span className="button-arrow" aria-hidden="true">{isLoading ? '···' : '↗'}</span>
      </button>
      <p className="form-footnote"><span aria-hidden="true">◈</span> Your numbers are only sent to calculate this estimate.</p>
    </form>
  )
}
