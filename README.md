# QA Automation Test Suite — Flamingo Assignment

## Prerequisites

- Java 21+
- Maven 3.6+
- Internet access (tests call live public APIs)

Playwright browsers are downloaded automatically on first run via the Maven plugin.

## How to Run

```bash
# Install Playwright browsers (first time only)
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

# Run all tests
mvn clean test

# Run only API tests
mvn test -Dgroups="api"

# Run only UI tests
mvn test -Dgroups="ui"

# Run only integration test
mvn test -Dgroups="integration"

# Generate Allure report
mvn allure:serve
```

## Project Structure

```
src/test/java/
├── api/
│   ├── tests/
│   │   ├── AuthenticationTest.java   # Auth positive + negative tests
│   │   ├── BookingApiTest.java       # Full CRUD + search + schema validation
│   │   └── GraphQlTest.java          # Rick and Morty GraphQL positive + negative tests
│   ├── models/
│   │   ├── Booking.java
│   │   ├── BookingDates.java
│   │   └── AuthResponse.java
│   └── helpers/
│       └── ApiHelper.java            # Shared specs, token fetch, factory methods
├── ui/
│   ├── tests/
│   │   ├── FormTest.java             # Student registration form tests
│   │   └── WebTableTest.java         # Web table CRUD + search + sort
│   ├── pages/
│   │   ├── FormPage.java             # POM for automation-practice-form
│   │   └── WebTablePage.java         # POM for webtables
│   └── base/
│       └── BaseUITest.java           # Playwright lifecycle, screenshot helper
├── integration/
│   └── ApiUiIntegrationTest.java     # API booking → UI table verification
└── config/
    └── TestConfig.java               # Central config loaded from test.properties
```

## Test Strategy

**API Tests (REST Assured + JUnit 5)**

Tested the Restful Booker API with a full CRUD lifecycle run in `@TestMethodOrder` to avoid test isolation issues on a shared public server. Authentication tests verify both positive and negative paths. GraphQL tests against the Rick and Morty API cover pagination, variables, nested fields across types, and all three negative cases (invalid ID, malformed query, non-existent field).

**UI Tests (Playwright + POM)**

Chose Option A (Form + Web Tables) as the recommended path. `BaseUITest` owns the Playwright/Browser lifecycle; each test gets a fresh `BrowserContext` to prevent state leakage. `FormPage` and `WebTablePage` encapsulate all selectors, keeping tests readable and resilient to minor DOM changes.

**Integration Test**

Creates a real booking via the Restful Booker API, takes `firstname`/`lastname`/`totalprice` from the response, and adds a matching record to the DemoQA Web Tables UI. Verifies the record is visible, then deletes the API booking in `@AfterAll` and confirms the 404.

**Prioritization:** CRUD lifecycle and schema validation first (highest risk), then negative/edge cases, then GraphQL.

## Challenges & Solutions

| Challenge | Solution |
|---|---|
| Restful Booker resets data periodically | Tests create their own data in `@BeforeAll` and clean up in `@AfterAll` |
| DemoQA ads obscure submit button | `scrollIntoViewIfNeeded()` before clicking |
| GraphQL invalid ID returns 200 (not 404) | Assertion checks `data.character == null \|\| errors != null` |
| `@TestMethodOrder` needed for CRUD sequence | Used `@Order` on booking tests to share `bookingId` state |

## What I Would Add With More Time

- **Partial update test** — `PATCH /booking/{id}` with `Cookie` token
- **Data-driven tests** — `@ParameterizedTest` with multiple booking payloads (boundary values, special chars)
- **Custom retry extension** — JUnit 5 extension to retry flaky network calls up to 3 times
- **Parallel execution** — separate Playwright contexts per thread for UI tests
- **CI/CD** — GitHub Actions workflow (`mvn test` on push + PR, Allure report published to Pages)
- **Contract testing** — Pact or JSON Schema validation for all API responses
- **More negative UI scenarios** — empty form submission, mobile number too short
