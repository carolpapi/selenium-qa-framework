package io.github.carolpapi.core;

import io.github.carolpapi.core.config.Config;
import io.github.carolpapi.core.driver.DriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementClickInterceptedException;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

/**
 * Parent of every Page Object. Centralizes synchronization and element interaction,
 * so the "how to wait" decision is made once instead of in hundreds of tests.
 *
 * Rules enforced here:
 *  - No Thread.sleep. Ever. We wait for a CONDITION, not for TIME.
 *  - Every interaction re-finds the element by its locator, so React re-renders
 *    (which replace DOM nodes) do not cause StaleElementReferenceException.
 *  - Each page declares an "anchor" element that proves it has finished loading.
 *    In a SPA, document.readyState is "complete" long before the content is there,
 *    so page-level readiness must be defined by the page itself.
 */
public abstract class BasePage {

    protected final WebDriver driver;

    protected BasePage() {
        this.driver = DriverManager.getDriver();
    }

    /**
     * A fresh explicit wait for each call. FluentWait is mutable (withMessage/ignoring
     * change the instance), so sharing one instance would leak settings between calls.
     */
    protected WebDriverWait await(String timeoutMessage) {
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(Config.getInt("timeout.explicit")));
        // React may replace a node between "find" and "use": retry instead of failing.
        wait.ignoring(StaleElementReferenceException.class);
        wait.withMessage(timeoutMessage);
        return wait;
    }

    /** Element whose visibility proves this page is ready to be used. */
    protected abstract By pageAnchor();

    /** Blocks until the page is usable. Called by navigation methods before returning a page. */
    @SuppressWarnings("unchecked")
    public <T extends BasePage> T waitUntilLoaded() {
        await(getClass().getSimpleName() + " did not load")
                .until(ExpectedConditions.visibilityOfElementLocated(pageAnchor()));
        return (T) this;
    }

    public boolean isLoaded() {
        return isDisplayed(pageAnchor());
    }

    // ---------- Locator helpers ----------

    /**
     * Preferred locator strategy: a dedicated test attribute agreed with developers.
     * It does not change when CSS classes, text or layout change.
     * SauceDemo uses "data-test"; many React teams use "data-testid".
     */
    protected static By testId(String value) {
        return By.cssSelector("[data-test='" + value + "']");
    }

    // ---------- Interactions (all synchronized) ----------

    protected WebElement waitVisible(By locator) {
        return await("Element not visible: " + locator)
                .until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    protected void click(By locator) {
        // Find + click inside the same wait loop: if the node goes stale, an animation
        // overlay intercepts the click or the element is still off-screen (sliding menu),
        // we simply try again until the timeout.
        await("Could not click " + locator)
                .ignoring(ElementClickInterceptedException.class)
                .ignoring(ElementNotInteractableException.class)
                .until(d -> {
                    WebElement element = d.findElement(locator);
                    if (!element.isDisplayed() || !element.isEnabled()) {
                        return false;
                    }
                    element.click();
                    return true;
                });
    }

    protected void type(By locator, String text) {
        WebElement input = waitVisible(locator);
        // React "controlled inputs" keep their value in component state. element.clear()
        // changes the DOM but may not fire onChange, so React restores the old value.
        // Selecting all + deleting through the keyboard behaves like a real user.
        Keys modifier = System.getProperty("os.name").toLowerCase().contains("mac") ? Keys.COMMAND : Keys.CONTROL;
        input.sendKeys(Keys.chord(modifier, "a"), Keys.DELETE);
        if (text != null && !text.isEmpty()) {
            input.sendKeys(text);
        }
    }

    protected String getText(By locator) {
        return waitVisible(locator).getText().trim();
    }

    /** Reads a list of texts, retrying as a whole if React re-renders the list mid-read. */
    protected List<String> getTexts(By locator) {
        return await("No elements found: " + locator).until(d -> {
            List<WebElement> elements = d.findElements(locator);
            if (elements.isEmpty()) {
                return null; // keep waiting
            }
            return elements.stream().map(e -> e.getText().trim()).toList();
        });
    }

    /** Non-blocking check: use for "is it there right now?" questions, never for waiting. */
    protected boolean isDisplayed(By locator) {
        List<WebElement> elements = driver.findElements(locator);
        try {
            return !elements.isEmpty() && elements.get(0).isDisplayed();
        } catch (StaleElementReferenceException e) {
            return false;
        }
    }

    protected void waitForText(By locator, String expectedText) {
        await("Expected text '" + expectedText + "' in " + locator)
                .until(ExpectedConditions.textToBePresentInElementLocated(locator, expectedText));
    }

    protected void waitUntilGone(By locator) {
        await("Element still visible: " + locator)
                .until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    protected void open(String path) {
        driver.get(Config.get("base.url") + path);
    }

    public String currentUrl() {
        return driver.getCurrentUrl();
    }
}
