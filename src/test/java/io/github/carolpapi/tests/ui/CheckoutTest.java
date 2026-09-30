package io.github.carolpapi.tests.ui;

import io.github.carolpapi.core.SessionHelper;
import io.github.carolpapi.data.CustomerInfo;
import io.github.carolpapi.data.TestDataFactory;
import io.github.carolpapi.data.User;
import io.github.carolpapi.pages.CartPage;
import io.github.carolpapi.pages.CheckoutCompletePage;
import io.github.carolpapi.pages.CheckoutInformationPage;
import io.github.carolpapi.pages.CheckoutOverviewPage;
import io.github.carolpapi.tests.base.BaseUiTest;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.testng.Assert.assertEquals;

/**
 * User story: "As a customer, I want to pay for the products in my cart,
 * so that I receive my order."
 *
 * Checkout is the highest business-risk flow (it is where revenue happens),
 * which is why it is automated first and is part of the smoke suite.
 */
public class CheckoutTest extends BaseUiTest {

    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final String BOLT_SHIRT = "Sauce Labs Bolt T-Shirt";

    @Test(groups = {"smoke", "ui"}, description = "E2E: customer buys two products successfully")
    public void customerCompletesPurchase() {
        CartPage cart = SessionHelper.loginViaSession(User.STANDARD)
                .addToCart(BACKPACK)
                .addToCart(BOLT_SHIRT)
                .header()
                .openCart();

        assertEquals(cart.getItemNames(), List.of(BACKPACK, BOLT_SHIRT));

        CheckoutCompletePage complete = cart.checkout()
                .continueWith(TestDataFactory.validCustomer())
                .finish();

        assertEquals(complete.getConfirmationMessage(), "Thank you for your order!");
    }

    @Test(groups = {"regression", "ui"}, description = "Business rule: item total = sum of item prices; total = item total + tax")
    public void orderTotalsAreCalculatedCorrectly() {
        CheckoutOverviewPage overview = SessionHelper.loginViaSession(User.STANDARD)
                .addToCart(BACKPACK)
                .addToCart(BOLT_SHIRT)
                .header()
                .openCart()
                .checkout()
                .continueWith(TestDataFactory.validCustomer());

        BigDecimal sumOfItems = overview.getItemPrices().stream().reduce(BigDecimal.ZERO, BigDecimal::add);

        assertEquals(overview.getSubtotal(), sumOfItems, "Item total should equal the sum of item prices");
        assertEquals(overview.getTotal(), overview.getSubtotal().add(overview.getTax()),
                "Total should equal item total + tax");
    }

    @Test(groups = {"regression", "ui"}, description = "Negative: postal code is mandatory")
    public void postalCodeIsRequired() {
        CustomerInfo customerWithoutZip = TestDataFactory.validCustomer().withPostalCode("");

        CheckoutInformationPage info = SessionHelper.loginViaSession(User.STANDARD)
                .addToCart(BACKPACK)
                .header()
                .openCart()
                .checkout()
                .continueExpectingError(customerWithoutZip);

        assertEquals(info.getErrorMessage(), "Error: Postal Code is required");
    }

    @Test(groups = {"regression", "ui"}, description = "Negative: first name is mandatory")
    public void firstNameIsRequired() {
        CustomerInfo customerWithoutName = TestDataFactory.validCustomer().withFirstName("");

        CheckoutInformationPage info = SessionHelper.loginViaSession(User.STANDARD)
                .addToCart(BACKPACK)
                .header()
                .openCart()
                .checkout()
                .continueExpectingError(customerWithoutName);

        assertEquals(info.getErrorMessage(), "Error: First Name is required");
    }
}
