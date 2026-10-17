# Cymru — Wales mortgage reality check

A full-stack mortgage-affordability dashboard. Enter your income, deposit, and
debts to get a personal borrowing estimate, then compare it with average sale
prices from HM Land Registry transactions in Welsh (and border) postcode areas.

- **Frontend:** React 18 + TypeScript, built with Vite 8 and tested with Vitest
- **Backend:** Spring Boot 4 REST API on Java 25, with Spring Data JPA and caching
- **Database:** PostgreSQL, run locally with Docker Compose

> This is an educational planning tool, not a mortgage offer, financial advice, or
> a property valuation. The estimates use simplified assumptions. Real lenders
> apply their own affordability rules, expense checks, credit assessments, and rates.

## Contents

- [How it works](#how-it-works)
- [Requirements](#requirements)
- [Quick start](#quick-start)
- [Configuration](#configuration)
- [Property data](#property-data)
- [API reference](#api-reference)
- [Testing](#testing)
- [Project layout](#project-layout)
- [Known limitations](#known-limitations)
- [Troubleshooting](#troubleshooting)
- [AI-assisted development](#ai-assisted-development)

## How it works

```text
┌──────────────────────┐   POST /api/v1/mortgages/calculate   ┌──────────────────────┐        ┌────────────┐
│ React dashboard      │ ───────────────────────────────────▶ │ Spring Boot API      │ ─────▶ │ PostgreSQL │
│ (Vite, port 5173)    │ ◀─────────────────────────────────── │ (port 8080)          │  JPA   │            │
│ local estimate + UI  │   area prices, schedule, LTV, etc.   │ calculations + cache │        │            │
└──────────────────────┘                                      └──────────────────────┘        └────────────┘
```

**The calculation** (the same logic runs in the browser and in the API):

1. Usable income = annual salary − (monthly debt × 12)
2. Maximum loan = usable income × **4.5** (zero if usable income is not positive)
3. Maximum purchase price = maximum loan + deposit
4. Loan-to-value = maximum loan ÷ maximum purchase price × 100
5. Monthly repayment uses the standard repayment-mortgage formula, or straight-line
   repayment when the interest rate is 0%.

**What updates when:**

- The personal estimate cards (budget, monthly repayment, LTV) are calculated in
  the browser and update immediately as you edit the form or move the sliders.
- Market data (area average, sales count, price by property type) and the
  year-by-year amortization chart come from the API. They refresh only when you
  press **See my reality check**.
- If you change the postcode area or property type after a request, the old market
  comparison is hidden until you run the check again. If the API is unreachable,
  an error banner is shown and the personal estimate stays visible.

## Requirements

| Tool           | Version                                         |
| -------------- | ----------------------------------------------- |
| Node.js + npm  | Node 22.12 or newer (required by Vite 8)        |
| JDK            | 25 (set by `java.version` in `backend/pom.xml`) |
| Docker Compose | Any recent version, to run PostgreSQL locally   |

You don't need to install Maven. The backend ships with a Maven wrapper
(`mvnw` / `mvnw.cmd`) that downloads Maven 3.9.12 on first use.

The frontend can be run and tested on its own. The backend and a database are only
needed for live area prices and the amortization chart.

## Quick start

### 1. Start PostgreSQL

From the repository root:

```sh
docker compose up -d postgres
docker compose port postgres 5432
```

Compose publishes PostgreSQL on a **randomly assigned host port**. The second
command prints it (for example `0.0.0.0:54321`). Use that port as `DB_PORT` below.

The database name, user, and password in `compose.yaml` are `mydatabase`, `myuser`,
and `secret`. They are for local development only.

### 2. Start the API

**PowerShell**

```powershell
$env:DB_HOST = "localhost"
$env:DB_PORT = "54321"   # the port printed in step 1
$env:DB_NAME = "mydatabase"
$env:DB_USER = "myuser"
$env:DB_PASSWORD = "secret"
cd backend
.\mvnw.cmd spring-boot:run
```

**macOS/Linux**

```sh
export DB_HOST=localhost
export DB_PORT=54321     # the port printed in step 1
export DB_NAME=mydatabase
export DB_USER=myuser
export DB_PASSWORD=secret
cd backend
./mvnw spring-boot:run
```

The API listens on `http://localhost:8080`. On startup, Hibernate creates or
updates the `property_transactions` table (`ddl-auto=update`).

### 3. Start the frontend

In a second terminal:

```sh
cd frontend
npm ci
npm run dev
```

Vite serves the app at `http://localhost:5173` and opens it in your browser.

The dashboard works at this point, but area averages will be empty until you load
property data (see [Property data](#property-data)).

## Configuration

### Backend

The backend reads its database connection from environment variables
(see `backend/src/main/resources/application.properties`):

| Variable      | Purpose                  | Example      |
| ------------- | ------------------------ | ------------ |
| `DB_HOST`     | PostgreSQL host          | `localhost`  |
| `DB_PORT`     | PostgreSQL port          | `54321`      |
| `DB_NAME`     | Database name            | `mydatabase` |
| `DB_USER`     | Database user            | `myuser`     |
| `DB_PASSWORD` | Database password        | `secret`     |

All five are required. Without them the application fails to start because the
datasource URL can't be resolved.

### Frontend

| Variable            | Default                     | Purpose          |
| ------------------- | --------------------------- | ---------------- |
| `VITE_API_BASE_URL` | `http://localhost:8080/api` | API base path    |

To change the default, copy `frontend/.env.example` to `frontend/.env` and edit it.
The client appends `/v1/mortgages/calculate` to this value. Restart `npm run dev`
after editing `.env`.

Don't put secrets in `VITE_*` variables. Vite embeds them in the JavaScript bundle
that is sent to the browser.

## Property data

The API reads **HM Land Registry Price Paid Data** from the `property_transactions`
table. The repository **does not include an importer or seed data**, so starting
Compose gives you an empty database. Load the data separately, for example by
downloading the
[Price Paid Data CSV](https://www.gov.uk/government/statistical-data-sets/price-paid-data-downloads)
and importing the relevant columns with `psql \copy` or a database client.

Table schema (from `PropertyTransaction.java`):

| Column           | Type      | Notes                                         |
| ---------------- | --------- | --------------------------------------------- |
| `transaction_id` | text (PK) | Land Registry transaction unique identifier   |
| `price`          | numeric   | Sale price, required                          |
| `transfer_date`  | timestamp | Date of transfer                              |
| `postcode`       | text      | Full postcode, e.g. `CF10 1AA`                |
| `property_type`  | text      | `D`, `S`, `T`, `F`, or `O` (see below)        |
| `town_city`      | text      |                                               |
| `county`         | text      |                                               |
| `street`         | text      |                                               |
| `district`       | text      |                                               |

Property type codes follow the Land Registry convention: **D** detached,
**S** semi-detached, **T** terraced, **F** flats/maisonettes, **O** other.

The dashboard offers these postcode areas: `CF`, `CH`, `HR`, `LD`, `LL`, `NP`, `SA`,
and `SY`. The API matches transactions whose postcode **starts with** the requested
area. It upper-cases the area first.

## API reference

### `POST /api/v1/mortgages/calculate`

Request:

```json
{
  "annualSalary": 65000,
  "depositAmount": 45000,
  "monthlyDebt": 250,
  "targetPostcodeArea": "CF",
  "propertyType": "T",
  "mortgageTermYears": 25,
  "annualInterestRate": 4
}
```

| Field                | Rules                                         |
| -------------------- | --------------------------------------------- |
| `annualSalary`       | required, ≥ 0                                 |
| `depositAmount`      | required, ≥ 0                                 |
| `monthlyDebt`        | required, ≥ 0                                 |
| `targetPostcodeArea` | required, non-blank                           |
| `propertyType`       | optional. Omit it or leave it empty for all types |
| `mortgageTermYears`  | required, integer ≥ 1                         |
| `annualInterestRate` | required, ≥ 0 (a percentage, e.g. `4` = 4%)   |

Invalid requests are rejected with `400 Bad Request`.

Response (illustrative values, schedule shortened):

```json
{
  "maxLoanAmount": 279000.00,
  "maxPurchasePrice": 324000.00,
  "averageAreaPrice": 265432.10,
  "estimatedMonthlyRepayment": 1472.67,
  "affordable": true,
  "totalSalesInArea": 18234,
  "loanToValueRatio": 86.11,
  "areaPriceBreakdown": { "D": 412000.00, "S": 255000.00, "T": 198000.00, "F": 165000.00 },
  "amortizationSchedule": [
    { "year": 1, "interestPaid": 11000.00, "principalPaid": 6672.04, "remainingBalance": 272327.96 }
  ]
}
```

The `amortizationSchedule` array has one entry per year of the term. `affordable`
is `true` when the maximum purchase price is at least the area average.

CORS is open to all origins (`@CrossOrigin(origins = "*")`) for local development.

Try it with curl:

```sh
curl -X POST http://localhost:8080/api/v1/mortgages/calculate \
  -H "Content-Type: application/json" \
  -d '{"annualSalary":65000,"depositAmount":45000,"monthlyDebt":250,"targetPostcodeArea":"CF","mortgageTermYears":25,"annualInterestRate":4}'
```

## Testing

### Frontend (Vitest + React Testing Library)

```sh
cd frontend
npm test             # run once
npm run test:watch   # watch mode
npm run build        # type-check (tsc) and build to frontend/dist/
npm run preview      # serve the production build locally
```

The tests cover:

- the 4.5× income calculation, debt, budget, and LTV
- repayment at positive and zero interest rates
- zero and negative borrowing edge cases
- currency formatting
- dashboard rendering and slider updates
- the API request payload
- loading and stale-result states
- API error handling

### Backend (JUnit 5 + Mockito)

```sh
cd backend
./mvnw test                                   # macOS/Linux
.\mvnw.cmd test                               # PowerShell
./mvnw "-Dtest=MortgageCalculatorServiceTest" test   # service tests only
```

- `MortgageCalculatorServiceTest` mocks the property-data layer, so it **doesn't
  need a database**. It covers borrowing, LTV, the amortization schedule,
  affordable and unaffordable comparisons, zero-interest repayment, and a zero
  loan.
- `PropertyMortgageApiApplicationTests` starts the full Spring context. It needs
  the `DB_*` variables and a running PostgreSQL instance (see
  [Quick start](#quick-start)).

## Project layout

```text
backend/                                  Spring Boot API
  src/main/java/com/dhar/propertymortgageapi/
    controller/MortgageController.java    POST /api/v1/mortgages/calculate
    service/MortgageCalculatorService     Borrowing, repayment, amortization maths
    service/PropertyDataService           Area averages, counts, breakdown (cached)
    repository/                           JPA queries on property_transactions
    model/PropertyTransaction.java        Land Registry transaction entity
    dto/                                  Request/response objects and validation
  src/main/resources/application.properties
  src/test/java/                          JUnit 5 tests
frontend/                                 React + Vite dashboard
  src/
    App.tsx                               Page layout and request state
    components/                           Form, price comparison, amortization chart
    lib/mortgage.ts                       In-browser estimate and currency formatting
    lib/options.ts                        Postcode areas, property types, terms
    services/mortgageApi.ts               API client
    types/mortgage.ts                     Request/response types
compose.yaml                              Local PostgreSQL service
```

## Known limitations

- **Simplified affordability.** The model uses a flat 4.5× loan-to-income multiple.
  It ignores living costs, credit history, stress-testing, and lender-specific LTV
  limits.
- **No data means "affordable".** If there are no transactions for an area, the
  average price comes back as `0`, so any budget is reported as affordable. Check
  that `totalSalesInArea` is greater than zero.
- **Cached averages.** Area averages and breakdowns are cached in memory for the
  life of the process. Restart the API after you load or change property data.
  Transaction counts are not cached.
- **Prefix matching.** Areas are matched with a `LIKE 'XX%'` prefix on the full
  postcode. A one-letter area would also match longer areas that start with the
  same letter.
- **Historical averages.** The figures are averages of past sales, not valuations
  or current asking prices.

## Troubleshooting

| Symptom | Likely cause |
| ------- | ------------ |
| *"The mortgage service is unavailable"* banner | The API isn't running, or `VITE_API_BASE_URL` points to the wrong host or port. |
| API fails with `Could not resolve placeholder 'DB_HOST'` | The `DB_*` environment variables aren't set in the terminal running `mvnw`. |
| API can't connect to PostgreSQL | `DB_PORT` doesn't match the port printed by `docker compose port postgres 5432`. The port changes whenever the container is recreated. |
| `release version 25 not supported` | Maven is running on an older JDK. Install JDK 25 and point `JAVA_HOME` at it. |
| Area average shows £0 and 0 sales | `property_transactions` is empty or has no rows for that area. See [Property data](#property-data). |
| New data doesn't appear | Averages are cached. Restart the API. |

## AI-assisted development

Development used GitHub Copilot in Visual Studio Code's **Agents window** (the
VS Code Chat experience with agent mode) to assist with frontend implementation,
automated tests, and documentation. AI assistance was used during development; it
is not part of the running application and is not required to build or run it.
Generated changes and test results should be reviewed by a developer before use.
