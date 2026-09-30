package io.github.carolpapi.core.driver;

import io.github.carolpapi.core.config.Config;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.Map;

/**
 * Creates browsers. The only class that knows HOW a browser is built,
 * so tests never change when we switch browser, go headless or move to a Grid.
 */
public final class DriverFactory {

    private DriverFactory() {
    }

    public static WebDriver create() {
        String browser = Config.get("browser").toLowerCase();
        boolean headless = Config.getBoolean("headless");
        String gridUrl = Config.get("grid.url", "");

        MutableCapabilities options = switch (browser) {
            case "firefox" -> firefoxOptions(headless);
            case "edge" -> edgeOptions(headless);
            case "chrome" -> chromeOptions(headless);
            default -> throw new IllegalArgumentException("Unsupported browser: " + browser);
        };

        WebDriver driver = gridUrl.isBlank() ? localDriver(browser, options) : remoteDriver(gridUrl, options);

        // Implicit wait stays at ZERO on purpose: mixing implicit + explicit waits
        // produces unpredictable timeouts. All synchronization lives in BasePage.
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(Config.getInt("timeout.pageload")));

        // Fixed viewport: a responsive layout that collapses in a small CI window is a
        // classic "passes locally, fails in the pipeline" cause.
        driver.manage().window().setSize(new Dimension(1920, 1080));
        return driver;
    }

    private static WebDriver localDriver(String browser, MutableCapabilities options) {
        return switch (browser) {
            case "firefox" -> new FirefoxDriver((FirefoxOptions) options);
            case "edge" -> new EdgeDriver((EdgeOptions) options);
            default -> new ChromeDriver((ChromeOptions) options);
        };
    }

    private static WebDriver remoteDriver(String gridUrl, MutableCapabilities options) {
        try {
            return new RemoteWebDriver(URI.create(gridUrl).toURL(), options);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Invalid grid.url: " + gridUrl, e);
        }
    }

    private static ChromeOptions chromeOptions(boolean headless) {
        ChromeOptions options = new ChromeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments(
                "--window-size=1920,1080",
                "--disable-dev-shm-usage",   // small /dev/shm in Docker/CI containers crashes Chrome
                "--no-sandbox",              // required when running as root in some CI images
                "--disable-notifications");
        // Chrome's password-manager / "password found in data breach" popup blocks the
        // page after login on demo sites. It appears on some machines and not others -
        // a real-world example of an environment-dependent flaky test.
        options.setExperimentalOption("prefs", Map.of(
                "credentials_enable_service", false,
                "profile.password_manager_enabled", false,
                "profile.password_manager_leak_detection", false));
        return options;
    }

    private static FirefoxOptions firefoxOptions(boolean headless) {
        FirefoxOptions options = new FirefoxOptions();
        if (headless) {
            options.addArguments("-headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return options;
    }

    private static EdgeOptions edgeOptions(boolean headless) {
        EdgeOptions options = new EdgeOptions();
        if (headless) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--window-size=1920,1080", "--disable-dev-shm-usage");
        return options;
    }
}
