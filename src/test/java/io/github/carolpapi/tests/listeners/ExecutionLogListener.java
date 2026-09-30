package io.github.carolpapi.tests.listeners;

import org.testng.ISuite;
import org.testng.ISuiteListener;
import org.testng.ITestListener;
import org.testng.ITestResult;

/**
 * Readable console output for CI logs: which test, which thread, how long.
 * The thread name makes parallel-execution problems much easier to diagnose.
 */
public class ExecutionLogListener implements ITestListener, ISuiteListener {

    @Override
    public void onStart(ISuite suite) {
        System.out.printf("%n=== Suite '%s' | env=%s | browser=%s | headless=%s ===%n",
                suite.getName(),
                System.getProperty("env", "qa"),
                System.getProperty("browser", "(from config)"),
                System.getProperty("headless", "(from config)"));
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        log("PASS", result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        log("FAIL", result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        log("SKIP", result);
    }

    private void log(String status, ITestResult result) {
        System.out.printf("[%s] %-60s %6d ms  (%s)%n",
                status,
                result.getTestClass().getRealClass().getSimpleName() + "." + result.getName(),
                result.getEndMillis() - result.getStartMillis(),
                Thread.currentThread().getName());
    }
}
