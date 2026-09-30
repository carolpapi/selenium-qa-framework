# Test Automation Strategy

## 1. What to automate first (when most testing is still manual)

I don't start by automating "everything". I start with the tests that **give back the most time and reduce the most risk**, and I use a simple scoring model so the decision is transparent to the team and the PO.

| Criterion | Question | Score |
|---|---|---|
| **Business impact** | If this breaks in production, do we lose money, customers or legal compliance? | 1-5 |
| **Frequency of execution** | How often is it tested manually (every release? every sprint? every PR?) | 1-5 |
| **Risk / change rate** | Does this area change often or has it had bugs before? | 1-5 |
| **Stability of the feature** | Is the UI/API stable enough, or is it being redesigned next month? | 1-5 |
| **Effort to automate** | Is it cheap (5) or very expensive (1) to automate and maintain? | 1-5 |

**Priority = Impact × Frequency × Risk, then Stability and Effort decide between ties.**

Applied to this project:

| Scenario | Impact | Frequency | Risk | Stability | Effort | Decision |
|---|---|---|---|---|---|---|
| Checkout end-to-end | 5 | 5 | 4 | 5 | 4 | **Automate first (smoke)** |
| Login (valid / blocked) | 5 | 5 | 3 | 5 | 5 | **Automate first (smoke)** |
| Order totals / tax rule | 5 | 4 | 4 | 5 | 5 | **API or UI regression** |
| Booking API CRUD + auth | 4 | 5 | 4 | 5 | 5 | **API regression** |
| Product sorting | 3 | 3 | 2 | 5 | 5 | Regression |
| Visual layout of product cards | 2 | 2 | 2 | 3 | 2 | Keep manual / visual tool later |
| One-off campaign page | 2 | 1 | 2 | 1 | 3 | **Do not automate** |

**Roadmap I usually propose:**
1. **Weeks 1-2:** smoke suite of the 5-10 critical journeys running in CI on every PR. Quick visible value.
2. **Next:** API tests for business rules (fast and stable), moving checks *down* the pyramid.
3. **Then:** grow UI regression for the rest of the manual regression checklist, ordered by the score above.
4. **Always:** manual testers move to exploratory testing, new features and usability, where humans add the most value.

## 2. Test pyramid

```
        /  UI E2E  \        few: critical user journeys (this repo: ui/)
       / API / service\      many: business rules, validations, auth, contracts (api/)
      /  unit (devs)   \     most: owned by developers
```

Rule of thumb: **if a rule can be verified by the API, it is tested by the API.** The UI suite checks that the pieces are connected and UI-only behavior.

## 3. Suites and when they run

| Suite | Content | When | Target time |
|---|---|---|---|
| `smoke` | critical journeys, API + UI | every PR / push | < 5 min |
| `api` | all API tests | every PR / push | < 2 min |
| `ui` | all UI tests | nightly, cross-browser | < 20 min (parallel) |
| `regression` | everything | before release | < 30 min |

## 4. Metrics to show the value of automation

| Metric | How to measure | Why it matters |
|---|---|---|
| **Manual regression time saved** | hours of manual regression per release before vs after | Easiest way to show ROI to management |
| **Automated coverage of the regression checklist** | automated cases / total regression cases | Shows progress of the roadmap |
| **Critical-journey coverage** | critical flows with automated smoke / total critical flows | Risk coverage, not just count |
| **Pipeline duration** | CI job time (trend) | Fast feedback = devs actually wait for it |
| **Pass rate / failure rate** | per run and trend | Health of product and suite |
| **Flaky rate** | tests that passed only after retry / total (from `[FLAKY]` logs) | Trust in the suite; target < 2% |
| **Bugs found by automation** | bugs caught before prod by the pipeline | Direct value |
| **Escaped defects** | bugs found in production in automated areas | Shows gaps in coverage |
| **MTTR of a red build** | time from failure to fix | Team discipline around quality |

I report these in a short dashboard or sprint review slide. Trends matter more than absolute numbers.
