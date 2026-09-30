package io.github.carolpapi.pages;

import io.github.carolpapi.core.BasePage;
import io.github.carolpapi.data.User;
import org.openqa.selenium.By;

/**
 * Page Object = what the page OFFERS (login, read error), not how it is built.
 * Tests call loginAs(...) and never see a locator, so a UI change is fixed here, once.
 */
public class LoginPage extends BasePage {

    private static final By USERNAME = testId("username");
    private static final By PASSWORD = testId("password");
    private static final By LOGIN_BUTTON = testId("login-button");
    private static final By ERROR_MESSAGE = testId("error");

    @Override
    protected By pageAnchor() {
        return LOGIN_BUTTON;
    }

    public LoginPage open() {
        open("/");
        return waitUntilLoaded();
    }

    /** Happy path: returns the NEXT page, which makes test flows fluent and type-safe. */
    public InventoryPage loginAs(User user) {
        submit(user.username(), user.password());
        return new InventoryPage().waitUntilLoaded();
    }

    /** Negative path: we stay on the login page and expect an error. */
    public LoginPage loginExpectingError(String username, String password) {
        submit(username, password);
        waitVisible(ERROR_MESSAGE);
        return this;
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    private void submit(String username, String password) {
        type(USERNAME, username);
        type(PASSWORD, password);
        click(LOGIN_BUTTON);
    }
}
