package io.github.carolpapi.core;

import io.github.carolpapi.core.config.Config;
import io.github.carolpapi.core.driver.DriverManager;
import io.github.carolpapi.data.User;
import io.github.carolpapi.pages.InventoryPage;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;

/**
 * Shortcut past the login screen.
 *
 * Login is covered by LoginTest. Every OTHER test (cart, checkout...) only needs
 * to BE logged in, so instead of typing credentials through the UI hundreds of
 * times we inject the session directly. In a real project this is usually an API
 * call that returns a token/cookie. Result: faster suites and fewer failures
 * unrelated to the feature under test.
 */
public final class SessionHelper {

    private SessionHelper() {
    }

    public static InventoryPage loginViaSession(User user) {
        WebDriver driver = DriverManager.getDriver();
        // A cookie can only be set for the domain currently loaded.
        driver.get(Config.get("base.url"));
        driver.manage().addCookie(new Cookie("session-username", user.username()));
        return new InventoryPage().open();
    }
}
