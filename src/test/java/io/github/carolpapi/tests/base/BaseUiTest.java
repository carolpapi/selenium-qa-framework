package io.github.carolpapi.tests.base;

import io.github.carolpapi.core.driver.DriverFactory;
import io.github.carolpapi.core.driver.DriverManager;
import io.qameta.allure.Allure;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Lifecycle for every UI test:
 *  - a NEW browser per test method -> no shared state (cookies, cart, local storage)
 *    between tests, so they can run in any order and in parallel.
 *  - on failure, collect evidence BEFORE closing the browser: screenshot, URL and
 *    page source. In CI there is nobody watching the screen, so evidence is everything.
 */
public abstract class BaseUiTest {

    @BeforeMethod(alwaysRun = true)
    public void startBrowser() {
        DriverManager.setDriver(DriverFactory.create());
    }

    @AfterMethod(alwaysRun = true)
    public void closeBrowser(ITestResult result) {
        try {
            if (result.getStatus() == ITestResult.FAILURE && DriverManager.hasDriver()) {
                captureEvidence(result);
            }
        } finally {
            DriverManager.quitDriver();
        }
    }

    private void captureEvidence(ITestResult result) {
        try {
            WebDriver driver = DriverManager.getDriver();
            String name = result.getTestClass().getRealClass().getSimpleName() + "." + result.getName();

            byte[] png = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot", "image/png", new ByteArrayInputStream(png), "png");
            Allure.addAttachment("URL", driver.getCurrentUrl());
            Allure.addAttachment("Page source", "text/html",
                    new ByteArrayInputStream(driver.getPageSource().getBytes(StandardCharsets.UTF_8)), "html");

            // Also saved as a plain file so the CI job can upload it as an artifact.
            Path dir = Path.of("target", "screenshots");
            Files.createDirectories(dir);
            Files.write(dir.resolve(name + ".png"), png);
        } catch (IOException | RuntimeException e) {
            // Never let evidence collection hide the real test failure.
            System.err.println("Could not capture evidence: " + e.getMessage());
        }
    }
}
