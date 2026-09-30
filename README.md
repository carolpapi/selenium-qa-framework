# Selenium QA Automation Framework

A UI + API test automation framework built to show **how I structure automation that scales to hundreds of tests**: maintainable, stable in CI and cheap to extend.

| | |
|---|---|
| **UI under test** | [SauceDemo](https://www.saucedemo.com), a React Single Page Application |
| **API under test** | [Restful-Booker](https://restful-booker.herokuapp.com), a REST API with auth |
| **Stack** | Java 17 · Selenium 4 · TestNG · RestAssured · Jackson · Datafaker · Allure · GitHub Actions · Selenium Grid (Docker) |

---

## How to run

```bash
mvn test                                   # full regression (default)
mvn test -Dsuite=smoke                     # critical journeys only
mvn test -Dsuite=api                       # API only, no browser
mvn test -Dsuite=ui -Dbrowser=firefox -Dheadless=true
mvn test -Denv=staging                     # another environment

# Selenium Grid (parallel, cross-browser, no local browsers needed)
docker compose up -d
mvn test -Dsuite=ui -Dgrid.url=http://localhost:4444

# Allure report (after a run)
allure serve target/allure-results
```

Every setting in `config/*.properties` can be overridden with `-Dkey=value` or an environment variable (`grid.url` → `GRID_URL`), so the same code runs locally, in CI and against any environment.

---

## Architecture

```
src/main/java/io/github/carolpapi
├── core/
│   ├── config/Config.java         # one source of config: -D > env var > {env}.properties
│   ├── driver/DriverFactory.java  # builds Chrome/Firefox/Edge, local or Grid, headless or not
│   ├── driver/DriverManager.java  # ThreadLocal<WebDriver> → safe parallel execution
│   ├── BasePage.java              # ALL waits and interactions live here (no Thread.sleep)
│   └── SessionHelper.java         # skips the login UI for tests that are not about login
├── pages/                         # Page Objects: what a page offers, not how it is built
│   └── components/HeaderComponent # reusable component shared by every logged-in page
├── api/                           # API clients (the "Page Object" idea for APIs) + models
└── data/                          # TestDataFactory, users, immutable test data records

src/test/java/io/github/carolpapi/tests
├── base/BaseUiTest.java           # browser per test + evidence on failure
├── listeners/                     # retry (tracked as FLAKY), execution log
├── ui/                            # Login, Inventory, Checkout
└── api/                           # Booking CRUD, auth, contract (JSON schema), negatives

src/test/resources/suites/         # smoke · api · ui · regression
.github/workflows/tests.yml        # CI pipeline
docker-compose.yml                 # Selenium Grid
docs/                              # strategy, flaky-test playbook, test case design
```

**Layering rule:** tests talk only to pages/clients → pages talk only to `BasePage` → only `BasePage` talks to Selenium synchronization. When the UI changes, I fix one Page Object; when the wait strategy needs tuning, I fix one class.

---

## Key design decisions

### 1. Page Object Model + components
- Page methods express **user intent** (`loginAs`, `addToCart`, `checkout`) and return the **next page**, so flows are fluent and the compiler catches invalid navigation.
- Tests contain **no locators and no Selenium calls**, only intent and assertions.
- Elements that repeat across pages (header, cart badge, menu) are **components**, modelled once.

### 2. Synchronization (the #1 source of flaky tests)
- Implicit wait is **0**. Only explicit waits, centralized in `BasePage`. Mixing both gives unpredictable timeouts.
- We wait for **conditions, never for time**: `waitForText(button, "Remove")` after adding to the cart, instead of `Thread.sleep(2000)`.
- `click()` re-finds the element inside the wait loop and retries on `StaleElementReference`, `ElementClickIntercepted` and `ElementNotInteractable`. That covers React re-renders and CSS animations (the side menu).
- `performance_glitch_user` (slow login) proves it: the test passes with no sleep and no extra time.

### 3. React / SPA specifics
- `document.readyState` is `complete` long before a SPA renders its content, so **each page declares an anchor element** (`pageAnchor()`) that proves it has loaded.
- React **re-renders replace DOM nodes**, so we never keep `WebElement` references; we keep `By` locators and re-locate.
- React **controlled inputs** can ignore `element.clear()`, so `type()` clears with the keyboard (Ctrl/Cmd+A, Delete), like a real user.
- Lists are read atomically and retried if they re-render during the read (`getTexts`).

### 4. Locator strategy
Priority order:
1. **Dedicated test attributes** (`data-test` / `data-testid`). They are stable and independent of styling and copy.
2. **ID**, when static and unique (`react-burger-menu-btn`).
3. **CSS selector** for structure.
4. **XPath** only when I need something CSS can't do, e.g. *"the button inside the card whose name is X"*:
   `//*[@data-test='inventory-item'][.//*[@data-test='inventory-item-name' and normalize-space()='Sauce Labs Backpack']]//button`

Never used: absolute XPaths, positional indexes (`//div[3]`), auto-generated classes (`css-1x2y3z`).

**What I ask developers for:** add `data-testid` to interactive elements and to list items (with a stable key like the product id), keep IDs unique and static, and expose loading states (`aria-busy`, spinners with a test id) so tests can wait for them. I usually do this as a short guideline doc plus a PR checklist item. The cost is close to zero if it's done while building the component.

### 5. Test data management
- `TestDataFactory` generates **unique data per run** (Datafaker), so there are no collisions between parallel threads or leftovers from old runs.
- Tests override only the field the scenario is about: `validCustomer().withPostalCode("")`.
- API tests **create their own data and delete it** at the end (`@AfterClass(alwaysRun = true)`), and never depend on records that "should already exist".
- UI tests that are not about login **inject the session** (`SessionHelper`) instead of logging in through the UI. That makes them faster and removes failures unrelated to the feature.

### 6. Parallelism
- `ThreadLocal<WebDriver>` gives one browser per thread, and a new browser per test, so there's no shared cookies/cart/state.
- Suites run `parallel="methods"`; data providers are parallel too.
- Selenium Grid (`docker-compose.yml`) scales the same code horizontally.

### 7. API vs UI
Business rules, validations, permissions and contracts are tested at the **API level**, where it's faster and more stable. The UI suite keeps **critical user journeys** and UI-only behavior (sorting, cart badge, form messages). See `docs/TEST_STRATEGY.md`.

### 8. CI/CD (`.github/workflows/tests.yml`)
| Trigger | What runs | Why |
|---|---|---|
| Pull request / push to main | API tests → UI smoke | Fast quality gate before merge |
| Nightly | API + full UI on Chrome **and** Firefox | Full regression, cross-browser |
| Manual | Any suite, any browser | Release checks, investigations |

- The API job runs first, and UI runs only if the API is healthy.
- Headless, fixed 1920×1080 viewport, `--disable-dev-shm-usage` for containers.
- Secrets come from GitHub Secrets as env vars, never from the repo.
- JUnit reports are published on the PR; Allure results, surefire reports and **failure screenshots** are uploaded as artifacts.
- Retries = 1 in CI, and every retry is printed as `[FLAKY]` and listed in the job, so it goes to the fix backlog instead of being hidden.

### 9. Evidence on failure
On failure, before the browser closes: screenshot, current URL and page source, attached to Allure and saved to `target/screenshots`. In CI nobody watches the screen, so evidence is what makes a failure diagnosable.

---

## Documentation
- [`docs/TEST_STRATEGY.md`](docs/TEST_STRATEGY.md): what to automate first, pyramid, metrics
- [`docs/FLAKY_TESTS.md`](docs/FLAKY_TESTS.md): playbook for "passes locally, fails in CI"
- [`docs/TEST_CASES.md`](docs/TEST_CASES.md): from user story to test scenarios
## Next steps
- Publish the Allure report to GitHub Pages on every nightly run
- Visual regression for `visual_user`
- Accessibility checks (axe-core) on critical pages
- Test impact analysis: run only the suites related to the changed code on PRs
