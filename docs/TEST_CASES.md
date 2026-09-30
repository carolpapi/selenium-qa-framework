# From User Story to Test Scenarios

## Approach
1. Read the story and the **acceptance criteria**; ask the PO about gaps *before* development (3 Amigos / refinement).
2. For each rule, derive: **positive** (happy path), **negative** (invalid input, missing permission), **boundary** (limits, empty, max length) and **alternative flows**.
3. Decide the **level** for each scenario: unit, API or UI (the cheapest level that proves the rule).
4. Decide **automate or not** (see `TEST_STRATEGY.md`).

---

## Example 1: Checkout

> **User story:** As a customer, I want to pay for the products in my cart, so that I receive my order.
>
> **Acceptance criteria**
> - AC1: First name, last name and postal code are mandatory.
> - AC2: The overview shows item total, tax and total; total = item total + tax.
> - AC3: After finishing, the customer sees a confirmation and the cart is empty.

| ID | Type | Scenario | Expected result | Level | Automated in |
|---|---|---|---|---|---|
| CO-01 | Positive | Customer with 2 items completes checkout with valid data | "Thank you for your order!" | UI (E2E) | `CheckoutTest.customerCompletesPurchase` |
| CO-02 | Business rule | Item total equals sum of item prices; total = item total + tax | Values match | UI (API if available) | `CheckoutTest.orderTotalsAreCalculatedCorrectly` |
| CO-03 | Negative | Postal code empty | "Error: Postal Code is required" | UI | `CheckoutTest.postalCodeIsRequired` |
| CO-04 | Negative | First name empty | "Error: First Name is required" | UI | `CheckoutTest.firstNameIsRequired` |
| CO-05 | Negative | Last name empty | "Error: Last Name is required" | UI | next to automate |
| CO-06 | Boundary | Very long names / special characters (ã, ç, emoji) | Accepted and displayed correctly | API/UI | backlog |
| CO-07 | Alternative | Customer cancels on overview | Returns to inventory, cart kept | UI | backlog |
| CO-08 | Edge | Checkout with empty cart | Should be blocked (question to PO) | UI | **open question** |

Gherkin example (used to align with PO/devs, not necessarily as code):

```gherkin
Scenario: Postal code is mandatory at checkout
  Given a logged-in customer with 1 product in the cart
  When they continue checkout without a postal code
  Then they see the error "Error: Postal Code is required"
  And they stay on the checkout information step
```

## Example 2: Login

> **User story:** As a customer, I want to log in with my credentials, so that I can buy products.

| ID | Type | Scenario | Expected | Automated in |
|---|---|---|---|---|
| LG-01 | Positive | Valid credentials | Product catalog displayed | `LoginTest.validUserCanLogIn` |
| LG-02 | Negative | Blocked account | "Sorry, this user has been locked out." | `LoginTest.lockedOutUserSeesError` |
| LG-03..06 | Negative | Empty user, empty password, wrong password, unknown user | Specific message per case | `LoginTest.invalidCredentialsShowValidationMessage` (data-driven) |
| LG-07 | Non-functional | Slow backend | Login completes, no false failure | `LoginTest.slowLoginIsHandledByExplicitWaits` |
| LG-08 | Positive | Logout | Back to login page | `LoginTest.userCanLogOut` |
