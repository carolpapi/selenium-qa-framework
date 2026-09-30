# Flaky Tests Playbook

> "It passes on my machine but fails in the pipeline."

## Step 1: Get evidence, don't guess
1. Open the CI artifacts: **screenshot, URL, page source** (`target/screenshots`, Allure attachments) and the console log.
2. Check if it failed **consistently** or **intermittently** (re-run history, `[FLAKY]` lines).
3. Reproduce locally **in CI conditions**: `mvn test -Dheadless=true -Dsuite=ui`, same browser, and ideally the same Docker image / Grid.
4. Run the test **alone** and **in parallel with the others**. If it only fails in parallel, it's a shared-state or data problem.

## Step 2: Find the category

| Symptom | Likely cause | Fix in this framework |
|---|---|---|
| `NoSuchElement` / `TimeoutException` right after an action | **Synchronization:** element not rendered yet (SPA, slow CI machine) | Explicit wait on the right condition (`BasePage`), page `pageAnchor()`; never `Thread.sleep` |
| `StaleElementReferenceException` | **React re-render** replaced the node | Keep `By`, not `WebElement`; re-locate inside the wait loop (`click()`, `getTexts()`) |
| `ElementClickInterceptedException` | Overlay / animation / cookie banner / spinner on top | Wait for overlay to disappear; retry click inside the wait (`click()`) |
| Works in headed, fails headless | **Viewport**: responsive layout hides the element (hamburger menu) | Fixed 1920×1080 in `DriverFactory` |
| Fails only in CI container, Chrome crashes | `/dev/shm` too small, sandbox | `--disable-dev-shm-usage`, `--no-sandbox`, `shm_size` in Grid |
| Random popup blocks the page | Browser feature (password manager, notifications) | Disable via browser options/prefs |
| Fails only when run with other tests | **Shared state/data**: same user, same record, static driver | `ThreadLocal` driver, new browser per test, unique data per test |
| Fails at certain times / days | Date/time zone, data that expires, test env maintenance | Relative dates (`LocalDate.now().plusDays`), fixed time zone, env health check |
| Fails because the data "is gone" | Tests depend on pre-existing data or on each other's order | Each test creates and cleans its own data (API setup/teardown) |
| Wrong URL / credentials in CI | **Environment configuration** | `Config` with env vars / secrets; print env at suite start (`ExecutionLogListener`) |
| Different result per browser | Browser-specific behavior | Cross-browser matrix nightly; locate the difference, report or adapt |
| Third-party service slow / down | External dependency | Mock/stub it in test env, or health-check and skip with clear reason |

## Step 3: Fix the root cause, then protect
- The fix goes in the **framework layer** (BasePage, DriverFactory, data factory) whenever possible, so it fixes every similar test, not just one.
- Retries (`retry.count=1` in CI) keep the pipeline usable, but **every retry is reported** as `[FLAKY]`. A test that needs a retry is on the backlog until fixed.
- If a test is flaky and can't be fixed right now, **quarantine** it (separate group, not blocking) with a ticket, rather than let the whole team learn to ignore red builds.
- If the flakiness is in the **product** (race condition, slow endpoint), it's a bug: report it with the evidence.
