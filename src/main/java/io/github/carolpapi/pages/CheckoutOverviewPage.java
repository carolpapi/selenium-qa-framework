package io.github.carolpapi.pages;

import io.github.carolpapi.core.BasePage;
import org.openqa.selenium.By;

import java.math.BigDecimal;
import java.util.List;

public class CheckoutOverviewPage extends BasePage {

    private static final By FINISH_BUTTON = testId("finish");
    private static final By ITEM_NAMES = testId("inventory-item-name");
    private static final By ITEM_PRICES = testId("inventory-item-price");
    private static final By SUBTOTAL = testId("subtotal-label");
    private static final By TAX = testId("tax-label");
    private static final By TOTAL = testId("total-label");

    @Override
    protected By pageAnchor() {
        return FINISH_BUTTON;
    }

    public List<String> getItemNames() {
        return getTexts(ITEM_NAMES);
    }

    public List<BigDecimal> getItemPrices() {
        return getTexts(ITEM_PRICES).stream().map(CheckoutOverviewPage::money).toList();
    }

    public BigDecimal getSubtotal() {
        return money(getText(SUBTOTAL));
    }

    public BigDecimal getTax() {
        return money(getText(TAX));
    }

    public BigDecimal getTotal() {
        return money(getText(TOTAL));
    }

    public CheckoutCompletePage finish() {
        click(FINISH_BUTTON);
        return new CheckoutCompletePage().waitUntilLoaded();
    }

    /** "Item total: $39.98" -> 39.98. BigDecimal, never double, for money. */
    private static BigDecimal money(String label) {
        return new BigDecimal(label.substring(label.indexOf('$') + 1).trim());
    }
}
