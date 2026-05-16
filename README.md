# Aelwyd — Welsh Property Affordability API

[![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?style=flat-square)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue?style=flat-square)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Compose-2496ED?style=flat-square)](https://docs.docker.com/compose/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey?style=flat-square)](LICENSE)

A REST API that calculates mortgage affordability, generates a full amortization schedule, and benchmarks buying power against live HM Land Registry property prices — all returned from a single unified endpoint.

> **Phase 1 (Backend) — Active Development.** Phase 2 (React UI) is in progress.

---

## Features

- **Affordability engine** — applies FCA standard 4.5× loan-to-income lending caps, factoring in monthly debt obligations and deposit size.
- **Amortization schedules** — month-by-month repayment breakdowns over a configurable term using `BigDecimal` for full financial precision.
- **Live market benchmarking** — cross-references calculated buying power against average prices from HM Land Registry Price Paid Data, filtered by Welsh postcode (e.g. `CF`, `SA`, `LL`).
- **Performance caching** — Spring `@Cacheable` in-memory caching cuts repeated SQL aggregation queries from ~50ms to ~2ms.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3.x |
| Database | PostgreSQL (via Spring Data JPA / Hibernate) |
| Infrastructure | Docker & Docker Compose |
| Utilities | Lombok, Spring Validation, Maven |

---

## Quick Start

### Prerequisites

- Java 21
- Maven
- Docker Desktop

### 1. Clone the repository

```bash
git clone https://github.com/your-username/aelwyd-property-api.git
cd aelwyd-property-api
```

### 2. Configure environment variables

Create a `.env` file in the project root:

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=aelwyd_db
DB_USER=postgres
DB_PASSWORD=your_secure_password

# Set to 'false' if using an external or homelab database
DOCKER_COMPOSE_ENABLED=true
```

### 3. Start the database

```bash
docker compose up -d
```

### 4. Run the application

```bash
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## API Reference

### `POST /api/v1/mortgages/calculate`

A unified endpoint that calculates maximum loan capacity, benchmarks it against local Welsh property averages by type, computes the Loan-to-Value (LTV) ratio, and returns a full year-by-year amortization schedule — all in a single request.

**Request body**

```json
{
  "annualSalary": 65000,
  "depositAmount": 25000,
  "monthlyDebt": 200,
  "targetPostcodeArea": "CF",
  "propertyType": "S",
  "mortgageTermYears": 25,
  "annualInterestRate": 4.5
}
```

| Field | Type | Required | Description |
|---|---|---|---|
| `annualSalary` | number | ✓ | Gross annual salary (GBP) |
| `depositAmount` | number | ✓ | Available cash deposit (GBP) |
| `monthlyDebt` | number | ✓ | Total monthly debt obligations (e.g. car finance, credit cards) |
| `targetPostcodeArea` | string | ✓ | Welsh postcode district (e.g. `CF`, `SA`, `NP`) |
| `propertyType` | string | | `D` (Detached), `S` (Semi-detached), `T` (Terraced), or `F` (Flat). Omit to search across all property types in the area. |
| `mortgageTermYears` | integer | ✓ | Total mortgage term in years |
| `annualInterestRate` | number | ✓ | Annual mortgage interest rate (%) |

**Response**

```json
{
  "maxLoanAmount": 282600.00,
  "maxPurchasePrice": 307600.00,
  "averageAreaPrice": 245000.00,
  "estimatedMonthlyRepayment": 1570.83,
  "isAffordable": true,
  "totalSalesInArea": 1450,
  "loanToValueRatio": 91.87,
  "areaPriceBreakdown": {
    "D": 380000.00,
    "S": 245000.00,
    "T": 190000.00,
    "F": 150000.00
  },
  "amortizationSchedule": [
    {
      "year": 1,
      "interestPaid": 12534.50,
      "principalPaid": 6315.46,
      "remainingBalance": 276284.54
    },
    {
      "year": 2,
      "interestPaid": 12244.57,
      "principalPaid": 6605.39,
      "remainingBalance": 269679.15
    }
  ]
}
```

---

## Project Structure

```
backend/
├── src/
│   └── main/
│       ├── java/com/dhar/propertymortgageapi/
│       │   ├── controller/       # REST controllers
│       │   ├── service/          # Business logic & affordability engine
│       │   ├── repository/       # Spring Data JPA repositories
│       │   ├── model/            # JPA entities
│       │   └── dto/              # Request & response DTOs
│       └── resources/
│           └── application.properties
├── compose.yaml
├── .env                          # Local environment config (not committed)
└── pom.xml
```

---

## Data Source

Property price data is sourced from the [HM Land Registry Price Paid Data](https://www.gov.uk/government/statistical-data-sets/price-paid-data-downloads), filtered to Welsh postcodes. The dataset is loaded into PostgreSQL via Docker on first run.

---

## License

[MIT](LICENSE)
