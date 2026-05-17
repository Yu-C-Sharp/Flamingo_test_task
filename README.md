# QA Automation Test Suite — Flamingo Assignment

## Prerequisites

- Java 21+
- Maven 3.6+
- Internet access (tests call live public APIs)

## How to Run

```bash
# Install Playwright browsers (required once before running UI tests)
mvn exec:java -e -Dexec.mainClass=com.microsoft.playwright.CLI -Dexec.args="install chromium" -Dexec.classpathScope=test

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
│   │   ├── booking/
│   │   │   ├── Booking.java
│   │   │   └── BookingDates.java
│   │   ├── book/
│   │   │   ├── Book.java
│   │   │   └── BooksResponse.java
│   │   └── auth/
│   │       └── AuthResponse.java
│   └── helpers/
│       └── ApiHelper.java            # Shared specs, token fetch, factory methods
├── ui/
│   ├── tests/
│   │   ├── FormTest.java             # Student registration form tests
│   │   └── WebTableTest.java         # Web table CRUD + search + sort
│   ├── pages/
│   │   ├── FormPage.java             # POM for automation-practice-form
│   │   ├── WebTablePage.java         # POM for webtables
│   │   └── BookStorePage.java        # POM for demoqa.com/books
│   └── base/
│       └── BaseUITest.java           # Playwright lifecycle, screenshots on every test
├── integration/
│   └── ApiUiIntegrationTest.java     # Book Store API → Book Store UI verification
└── config/
    └── TestConfig.java               # Central config loaded from test.properties
```

## Test Strategy

**API Tests (REST Assured + JUnit 5)**

Tested the Restful Booker API with a full CRUD lifecycle. Authentication tests verify both positive and negative paths. GraphQL tests against the Rick and Morty API cover pagination, variables, nested fields across types, and all three negative cases (invalid ID, malformed query, non-existent field).

**UI Tests (Playwright + POM)**

Chose Option A (Form + Web Tables) as the recommended path. `BaseUITest` owns the Playwright/Browser lifecycle; each test gets a fresh `BrowserContext` to prevent state leakage. `FormPage`, `WebTablePage`, and `BookStorePage` encapsulate all selectors, keeping tests readable and resilient to minor DOM changes. A screenshot is attached to the Allure report after every test.

UI tests use a **data-driven approach**: test input is loaded from JSON files via `BaseUITest.loadJsonData()` and deserialized into typed model objects, so adding or changing test scenarios requires only editing the JSON — no code changes needed.

**Integration Test**

Fetches the book list from the DemoQA Book Store API (`GET /BookStore/v1/Books`), picks the first book, then navigates to `demoqa.com/books` and verifies the book title is visible in the UI table.

**Prioritization:** CRUD lifecycle and schema validation first (highest risk), then negative/edge cases, then GraphQL.

## Challenges & Solutions

| Challenge | Solution |
|---|---|
| Restful Booker returns 418 for some requests | `RetryExtension` retries failing tests up to 3 times in CI; `checkOkStatusCode()` accepts 200 or 418 |
| Restful Booker resets data periodically | Tests create their own data in `@BeforeAll` and clean up in `@AfterAll` |
| DemoQA ads obscure clickable elements | `page.evaluate()` removes `#fixedban` and iframes after navigation; force click used where needed |
| GraphQL invalid ID returns 200 (not 404) | Assertion checks `data.character == null \|\| errors != null` |

## What I Would Add With More Time

- **Partial update test** — `PATCH /booking/{id}` with `Cookie` token
- **Data-driven API tests** — `@ParameterizedTest` with multiple booking payloads (boundary values, special chars)
- **Parallel execution** — separate Playwright contexts per thread for UI tests
- **Contract testing** — Pact or JSON Schema validation for all API responses
- **More negative UI scenarios** — empty form submission, mobile number too short
