package io.github.carolpapi.pages;

import io.github.carolpapi.core.BasePage;
import io.github.carolpapi.pages.components.HeaderComponent;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.Select;

import java.math.BigDecimal;
import java.util.List;

public class InventoryPage extends BasePage {

    private static final By INVENTORY_CONTAINER = testId("inventory-container");
    private static final By PAGE_TITLE = testId("title");
    private static final By PRODUCT_NAMES = testId("inventory-item-name");
    private static final By PRODUCT_PRICES = testId("inventory-item-price");
    private static final By SORT_DROPDOWN = testId("product-sort-container");

    /**
     * Dynamic list: products come from data, so we locate the CARD by the product
     * name and then the button INSIDE that card. This XPath is anchored on stable
     * test attributes + visible name - never on position (//div[3]/button), which
     * breaks as soon as the catalog order changes.
     */
    private static By productButton(String productName) {
        return By.xpath("//*[@data-test='inventory-item']"
                + "[.//*[@data-test='inventory-item-name' and normalize-space()='" + productName + "']]"
                + "//button");
    }

    private final HeaderComponent header = new HeaderComponent();

    @Override
    protected By pageAnchor() {
        return INVENTORY_CONTAINER;
    }

    public InventoryPage open() {
        open("/inventory.html");
        return waitUntilLoaded();
    }

    public HeaderComponent header() {
        return header;
    }

    public String getTitle() {
        return getText(PAGE_TITLE);
    }

    public InventoryPage addToCart(String productName) {
        By button = productButton(productName);
        click(button);
        // Synchronize on the STATE CHANGE we caused (button flips to "Remove"),
        // not on a fixed delay. React re-renders the button, so we re-locate it.
        waitForText(button, "Remove");
        return this;
    }

    public InventoryPage removeFromCart(String productName) {
        By button = productButton(productName);
        click(button);
        waitForText(button, "Add to cart");
        return this;
    }

    public InventoryPage sortBy(SortOption option) {
        new Select(waitVisible(SORT_DROPDOWN)).selectByValue(option.value());
        return this;
    }

    public List<String> getProductNames() {
        return getTexts(PRODUCT_NAMES);
    }

    public List<BigDecimal> getProductPrices() {
        return getTexts(PRODUCT_PRICES).stream()
                .map(price -> new BigDecimal(price.replace("$", "")))
                .toList();
    }

    public String getPriceOf(String productName) {
        By price = By.xpath("//*[@data-test='inventory-item']"
                + "[.//*[@data-test='inventory-item-name' and normalize-space()='" + productName + "']]"
                + "//*[@data-test='inventory-item-price']");
        return getText(price);
    }

    public enum SortOption {
        NAME_A_TO_Z("az"),
        NAME_Z_TO_A("za"),
        PRICE_LOW_TO_HIGH("lohi"),
        PRICE_HIGH_TO_LOW("hilo");

        private final String value;

        SortOption(String value) {
            this.value = value;
        }

        public String value() {
            return value;
        }
    }
}
