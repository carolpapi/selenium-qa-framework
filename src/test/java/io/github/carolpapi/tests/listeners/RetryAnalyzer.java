package io.github.carolpapi.tests.listeners;

import io.github.carolpapi.core.config.Config;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Retries a failed test up to "retry.count" times.
 *
 * IMPORTANT - this is a safety net, not a fix:
 *  - retry.count is 0 locally so flakiness is visible while developing;
 *  - in CI it is 1, and every retry is logged as [FLAKY] so the test goes to the
 *    flaky-test backlog and gets a root-cause fix (wait, data, environment...).
 * A test that only passes on retry is still a bug in the test or in the product.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private final int maxRetries = Config.getInt("retry.count", 0);
    private int attempt = 0;

    @Override
    public boolean retry(ITestResult result) {
        if (attempt < maxRetries) {
            attempt++;
            System.out.printf("[FLAKY] Retrying %s.%s (attempt %d of %d) - cause: %s%n",
                    result.getTestClass().getRealClass().getSimpleName(),
                    result.getName(), attempt, maxRetries,
                    result.getThrowable() == null ? "unknown" : result.getThrowable().getClass().getSimpleName());
            return true;
        }
        return false;
    }
}
