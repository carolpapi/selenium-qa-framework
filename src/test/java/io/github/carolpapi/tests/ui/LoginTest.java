package io.github.carolpapi.tests.ui;

import io.github.carolpapi.data.User;
import io.github.carolpapi.pages.InventoryPage;
import io.github.carolpapi.pages.LoginPage;
import io.github.carolpapi.tests.base.BaseUiTest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * User story: "As a customer, I want to log in with my credentials,
 * so that I can buy products."  (scenarios derived in docs/TEST_CASES.md)
 */
public class LoginTest extends BaseUiTest {

    @Test(groups = {"smoke", "ui"}, description = "Valid user logs in and sees the product catalog")
    public void validUserCanLogIn() {
        InventoryPage inventory = new LoginPage().open().loginAs(User.STANDARD);

        assertEquals(inventory.getTitle(), "Products");
    }

    @Test(groups = {"regression", "ui"}, description = "Blocked account cannot log in")
    public void lockedOutUserSeesError() {
        LoginPage login = new LoginPage().open()
                .loginExpectingError(User.LOCKED_OUT.username(), User.LOCKED_OUT.password());

        assertEquals(login.getErrorMessage(), "Epic sadface: Sorry, this user has been locked out.");
    }

    /**
     * Data-driven negative scenarios: one test method, many business rules.
     * Adding a new rule = adding one line, not a new test.
     */
    @DataProvider(name = "invalidCredentials", parallel = true)
    public Object[][] invalidCredentials() {
        return new Object[][]{
                {"", "secret_sauce", "Epic sadface: Username is required"},
                {"standard_user", "", "Epic sadface: Password is required"},
                {"standard_user", "wrong_password",
                        "Epic sadface: Username and password do not match any user in this service"},
                {"unknown_user", "secret_sauce",
                        "Epic sadface: Username and password do not match any user in this service"},
        };
    }

    @Test(dataProvider = "invalidCredentials", groups = {"regression", "ui"},
            description = "Invalid credentials show the right validation message")
    public void invalidCredentialsShowValidationMessage(String username, String password, String expectedError) {
        LoginPage login = new LoginPage().open().loginExpectingError(username, password);

        assertEquals(login.getErrorMessage(), expectedError);
    }

    /**
     * performance_glitch_user takes several seconds to log in. With Thread.sleep we
     * would either be flaky (sleep too short) or slow (sleep too long). The explicit
     * wait in waitUntilLoaded() returns as soon as the page is ready.
     */
    @Test(groups = {"regression", "ui"}, description = "Slow backend: explicit waits handle the delay")
    public void slowLoginIsHandledByExplicitWaits() {
        InventoryPage inventory = new LoginPage().open().loginAs(User.PERFORMANCE_GLITCH);

        assertTrue(inventory.isLoaded(), "Inventory should be loaded after a slow login");
    }

    @Test(groups = {"regression", "ui"}, description = "User can log out from the side menu")
    public void userCanLogOut() {
        LoginPage login = new LoginPage().open()
                .loginAs(User.STANDARD)
                .header()
                .logout();

        assertTrue(login.isLoaded(), "Should be back on the login page");
    }
}
