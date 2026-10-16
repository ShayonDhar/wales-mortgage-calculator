# Cymru — Wales mortgage reality check

A full-stack mortgage-affordability dashboard for exploring a personal borrowing
estimate and comparing it with property transaction averages for Welsh postcode
areas. The React frontend talks to a Spring Boot API backed by PostgreSQL.

This is an educational planning tool, not a mortgage offer, financial advice, or a
property valuation. The estimates use simplified assumptions; lenders use their own
affordability rules, expenses, credit assessments, and rates.

## What the application does

- Estimates maximum borrowing using 4.5 times annual income after subtracting
  annualised monthly debt payments.
- Adds the cash deposit to the estimated loan to show a maximum purchase budget.
- Recalculates the monthly repayment as the user changes the term or annual
  interest-rate scenario.
- Requests postcode-area averages, transaction counts, property-type averages, and
  an annual amortization schedule from the backend when the user submits the form.
- Compares the estimated budget to the selected area's average price.
- Displays API errors without hiding the locally calculated personal estimate.

The deposit and interest-rate sliders update the personal estimate immediately.
Market data and the backend amortization schedule are refreshed only when **See my
reality check** is submitted. If the postcode area or property type changes after a
successful request, the old market comparison is hidden until the next request.

## Technology

- React 18 and TypeScript
- Vite 8 for development and production builds
- Vitest 5, React Testing Library, and jsdom for frontend tests
- Java (the version configured in `backend/pom.xml`) and Spring Boot 4 for the API
- PostgreSQL with Spring Data JPA

## Requirements

- Node.js 22.12 or newer (the Vite 8 toolchain requirement)
- npm
- A JDK matching the Java version configured in `backend/pom.xml`
- Docker Compose and PostgreSQL for a complete local API/data setup

The frontend can be run and tested on its own. The backend and its database must be
running to load live area prices and amortization results.

## Frontend setup

From the repository root:

```sh
cd frontend
npm ci
```

Create `frontend/.env` if the API is not at the default address:

```dotenv
VITE_API_BASE_URL=http://localhost:8080/api
```

`VITE_API_BASE_URL` is the API base path; the frontend appends
`/v1/mortgages/calculate`. Restart Vite after changing environment variables. Do not
put secrets in frontend environment variables: Vite embeds `VITE_*` values in
browser code.

## Run the full application

1. Start the PostgreSQL service from the repository root:

   ```sh
   docker compose up -d postgres
   docker compose port postgres 5432
   ```

   The Compose service publishes PostgreSQL on a dynamically assigned host port.
   Use the port reported by `docker compose port` in `DB_PORT` below. The current
   Compose configuration supplies the local database name `mydatabase`, user
   `myuser`, and password `secret`; do not reuse these development credentials
   outside a local environment.

2. In a terminal at the repository root, configure the backend connection and
   start Spring Boot. Replace `54321` with the published host port from the
   previous step:

   **PowerShell**

   ```powershell
   $env:DB_HOST = "localhost"
   $env:DB_PORT = "54321"
   $env:DB_NAME = "mydatabase"
   $env:DB_USER = "myuser"
   $env:DB_PASSWORD = "secret"
   cd backend
   .\mvnw.cmd spring-boot:run
   ```

   **macOS/Linux**

   ```sh
   export DB_HOST=localhost
   export DB_PORT=54321
   export DB_NAME=mydatabase
   export DB_USER=myuser
   export DB_PASSWORD=secret
   cd backend
   ./mvnw spring-boot:run
   ```

   The API listens on port 8080 by default. If it is configured on another port,
   update `VITE_API_BASE_URL` to match.

3. In a second terminal, start the frontend:

   ```sh
   cd frontend
   npm ci
   npm run dev
   ```

4. Open the URL printed by Vite (normally `http://localhost:5173`).

### Property data prerequisite

The API expects HM Land Registry Price Paid Data to be loaded into the PostgreSQL
`property_transactions` table. Starting the Compose service creates an empty
database; it does **not** import or seed transaction records. The repository does
not currently include a data import/seed command, so market averages and
transaction counts require a separately provisioned dataset. With an empty table,
the frontend still displays the personal estimate and reports the lack of area
data. The API creates/updates its table schema through Hibernate.

## API contract

The frontend sends a JSON `POST` request to
`{VITE_API_BASE_URL}/v1/mortgages/calculate`, for example:

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

`propertyType` is omitted when the user chooses all property types. The response
contains the maximum loan and purchase price, average area price, estimated monthly
repayment, affordability flag, sales count, loan-to-value ratio, average prices by
property type, and a year-by-year amortization schedule. The UI accepts either
`isAffordable` or `affordable` for the affordability flag.

The backend currently accepts the postcode area as text and uses it to query
property records. The frontend offers Welsh and nearby postcode areas; available
results depend on which records have been loaded.

## Tests and production build

### Frontend

```sh
cd frontend
npm test
npm run test:watch
npm run build
```

`npm test` runs the suite once in Vitest/jsdom. Coverage includes:

- The 4.5× income calculation, annualised debt, purchase budget, and loan-to-value.
- Amortization formula behavior at positive and zero interest rates.
- Zero/negative borrowing edge cases and currency formatting.
- Initial dashboard estimates and immediate interest-rate scenario updates.
- API request URL, method, headers, and submitted input payload.
- Market comparison and yearly schedule rendering.
- Pending requests, clearing stale results, and API-error behavior.

`npm run build` runs the TypeScript compiler before generating optimized assets in
`frontend/dist/`. `npm run preview` serves that production build locally.

### Backend (JUnit 5)

The backend uses JUnit 5 with Mockito through the Spring Boot test dependencies.
Run the backend tests from `backend/`:

**PowerShell**

```powershell
cd backend
.\mvnw.cmd test
```

**macOS/Linux**

```sh
cd backend
./mvnw test
```

The suite includes a Spring application-context smoke test and isolated
`MortgageCalculatorServiceTest` unit tests. The service tests mock property-data
lookups, so they verify the affordability calculations without requiring a running
PostgreSQL database:

- 4.5× usable-income borrowing, deposit, LTV, area-data lookup, and amortization.
- Affordable and unaffordable area-price comparisons.
- Zero-interest straight-line repayments.
- Income exhausted by debt, zero loan, deposit-only budget, and empty schedule.

Run just the isolated service tests with:

**PowerShell**

```powershell
.\mvnw.cmd "-Dtest=MortgageCalculatorServiceTest" test
```

**macOS/Linux**

```sh
./mvnw "-Dtest=MortgageCalculatorServiceTest" test
```

The application-context smoke test starts the full Spring context and therefore
requires the backend environment variables and a reachable PostgreSQL database,
as described in [Run the full application](#run-the-full-application). Run
`.\mvnw.cmd test` or `./mvnw test` with that database available to run the complete
backend suite.

## Project layout

```text
backend/                         Spring Boot API and persistence
  src/test/java/                 JUnit 5 tests
frontend/
  src/
    components/                  Form, market comparison, amortization chart
    lib/mortgage.ts              Local estimate and currency formatting
    services/mortgageApi.ts      Backend HTTP client
    types/mortgage.ts            Shared frontend request/response types
    test/                        Test environment setup
    App.tsx                      Dashboard composition and request state
  vitest.config.ts               Vitest/jsdom configuration
  package.json                   Scripts and frontend dependencies
compose.yaml                     Local PostgreSQL service
```

## AI-assisted development

Development used GitHub Copilot in Visual Studio Code's **Agents window** (the
VS Code Chat experience with agent mode) to assist with frontend implementation,
automated tests, and documentation. AI assistance was used during development; it
is not part of the running application and is not required to build or run it.
Generated changes and test results should be reviewed by a developer before use.
