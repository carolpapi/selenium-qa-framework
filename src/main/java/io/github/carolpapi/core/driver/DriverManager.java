package io.github.carolpapi.core.driver;

import org.openqa.selenium.WebDriver;

/**
 * Holds one WebDriver per thread.
 *
 * Why ThreadLocal: when TestNG runs tests in parallel, each thread must own its
 * own browser. A shared static driver is the #1 cause of "works alone, fails in
 * parallel" flakiness (tests clicking in each other's browser).
 */
public final class DriverManager {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    private DriverManager() {
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER.get();
        if (driver == null) {
            throw new IllegalStateException("No WebDriver for this thread. Did the test extend BaseUiTest?");
        }
        return driver;
    }

    public static void setDriver(WebDriver driver) {
        DRIVER.set(driver);
    }

    public static boolean hasDriver() {
        return DRIVER.get() != null;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER.get();
        if (driver != null) {
            try {
                driver.quit();
            } finally {
                DRIVER.remove(); // avoid leaking drivers between tests on pooled threads
            }
        }
    }
}
