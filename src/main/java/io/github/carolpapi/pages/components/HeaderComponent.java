package io.github.carolpapi.pages.components;

import io.github.carolpapi.core.BasePage;
import io.github.carolpapi.pages.CartPage;
import io.github.carolpapi.pages.LoginPage;
import org.openqa.selenium.By;

/**
 * Component Object: the header (cart icon + side menu) appears on every logged-in page.
 * Modelling it once and composing it into pages avoids duplicating locators
 * across dozens of Page Objects - key to keeping hundreds of tests maintainable.
 */
public class HeaderComponent extends BasePage {

    private static final By CART_LINK = testId("shopping-cart-link");
    private static final By CART_BADGE = testId("shopping-cart-badge");
    // IDs are the fastest and most stable locators WHEN they are static and unique.
    private static final By MENU_BUTTON = By.id("react-burger-menu-btn");
    private static final By LOGOUT_LINK = By.id("logout_sidebar_link");

    @Override
    protected By pageAnchor() {
        return CART_LINK;
    }

    /** The badge is not rendered at all when the cart is empty - absence means 0. */
    public int getCartCount() {
        return isDisplayed(CART_BADGE) ? Integer.parseInt(getText(CART_BADGE)) : 0;
    }

    public void waitForCartCount(int expected) {
        if (expected == 0) {
            waitUntilGone(CART_BADGE);
        } else {
            waitForText(CART_BADGE, String.valueOf(expected));
        }
    }

    public CartPage openCart() {
        click(CART_LINK);
        return new CartPage().waitUntilLoaded();
    }

    public LoginPage logout() {
        click(MENU_BUTTON);
        // The side menu slides in with a CSS animation. Clicking during the animation
        // can hit the overlay (ElementClickInterceptedException). BasePage.click()
        // retries until the link is really clickable - no sleep needed.
        click(LOGOUT_LINK);
        return new LoginPage().waitUntilLoaded();
    }
}
