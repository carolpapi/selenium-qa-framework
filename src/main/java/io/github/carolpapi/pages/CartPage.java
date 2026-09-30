package io.github.carolpapi.pages;

import io.github.carolpapi.core.BasePage;
import org.openqa.selenium.By;

import java.util.List;

public class CartPage extends BasePage {

    private static final By CHECKOUT_BUTTON = testId("checkout");
    private static final By ITEM_NAMES = testId("inventory-item-name");

    @Override
    protected By pageAnchor() {
        return CHECKOUT_BUTTON;
    }

    public List<String> getItemNames() {
        // An empty cart is a valid state, so we don't wait for items here.
        return driver.findElements(ITEM_NAMES).stream().map(e -> e.getText().trim()).toList();
    }

    public CheckoutInformationPage checkout() {
        click(CHECKOUT_BUTTON);
        return new CheckoutInformationPage().waitUntilLoaded();
    }
}
