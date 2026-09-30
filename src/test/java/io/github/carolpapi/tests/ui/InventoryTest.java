package io.github.carolpapi.tests.ui;

import io.github.carolpapi.core.SessionHelper;
import io.github.carolpapi.data.User;
import io.github.carolpapi.pages.InventoryPage;
import io.github.carolpapi.pages.InventoryPage.SortOption;
import io.github.carolpapi.tests.base.BaseUiTest;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class InventoryTest extends BaseUiTest {

    private static final String BACKPACK = "Sauce Labs Backpack";
    private static final String BIKE_LIGHT = "Sauce Labs Bike Light";

    @Test(groups = {"smoke", "ui"}, description = "Adding products updates the cart badge")
    public void addingProductsUpdatesCartBadge() {
        InventoryPage inventory = SessionHelper.loginViaSession(User.STANDARD)
                .addToCart(BACKPACK)
                .addToCart(BIKE_LIGHT);

        inventory.header().waitForCartCount(2);
        assertEquals(inventory.header().getCartCount(), 2);
    }

    @Test(groups = {"regression", "ui"}, description = "Removing a product decreases the cart badge")
    public void removingProductDecreasesCartBadge() {
        InventoryPage inventory = SessionHelper.loginViaSession(User.STANDARD)
                .addToCart(BACKPACK)
                .addToCart(BIKE_LIGHT)
                .removeFromCart(BACKPACK);

        inventory.header().waitForCartCount(1);
        assertEquals(inventory.header().getCartCount(), 1);
    }

    @Test(groups = {"regression", "ui"}, description = "Sorting by price (low to high) orders the catalog")
    public void sortByPriceLowToHigh() {
        InventoryPage inventory = SessionHelper.loginViaSession(User.STANDARD)
                .sortBy(SortOption.PRICE_LOW_TO_HIGH);

        List<BigDecimal> prices = inventory.getProductPrices();
        List<BigDecimal> expected = prices.stream().sorted().toList();
        // Assert the RULE (list is sorted), not a hard-coded list of prices that
        // would break whenever the catalog changes.
        assertEquals(prices, expected, "Products should be sorted by price ascending");
    }

    @Test(groups = {"regression", "ui"}, description = "Sorting by name (Z to A) orders the catalog")
    public void sortByNameZToA() {
        InventoryPage inventory = SessionHelper.loginViaSession(User.STANDARD)
                .sortBy(SortOption.NAME_Z_TO_A);

        List<String> names = inventory.getProductNames();
        List<String> expected = names.stream().sorted(Comparator.reverseOrder()).toList();
        assertEquals(names, expected, "Products should be sorted by name descending");
    }
}
