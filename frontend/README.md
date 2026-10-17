# Frontend — Cymru mortgage dashboard

React 18 + TypeScript dashboard built with Vite 8. For the full project overview,
backend setup, API reference, and property-data notes, see the
[root README](../README.md).

## Commands

Requires Node.js 22.12 or newer.

```sh
npm ci               # install dependencies
npm run dev          # dev server at http://localhost:5173
npm test             # run the Vitest suite once
npm run test:watch   # run tests in watch mode
npm run build        # type-check with tsc, then build to dist/
npm run preview      # serve the production build
```

## Configuration

Copy `.env.example` to `.env` to point the app at a different API:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
```

The client appends `/v1/mortgages/calculate`. Restart Vite after changing `.env`.
`VITE_*` values end up in the browser bundle, so never put secrets in them.

## Structure

```text
src/
  App.tsx                    Page layout, form state, request lifecycle
  components/
    MortgageForm.tsx         Inputs, sliders, postcode area and property type
    PriceComparison.tsx      Budget vs. area average
    AmortizationChart.tsx    Yearly balance chart from the API schedule
  lib/mortgage.ts            In-browser estimate (4.5× income) and formatting
  lib/options.ts             Postcode areas, property types, mortgage terms
  services/mortgageApi.ts    fetch wrapper for the calculate endpoint
  types/mortgage.ts          Request/response and form types
  test/setup.ts              jest-dom matchers for Vitest
```

The personal estimate is calculated locally with `lib/mortgage.ts`, so it keeps
working when the API is down. Market data and the amortization chart need a
successful API response, and they are hidden once the inputs no longer match the
last request.
