package io.github.carolpapi.pages;

import io.github.carolpapi.core.BasePage;
import io.github.carolpapi.data.CustomerInfo;
import org.openqa.selenium.By;

public class CheckoutInformationPage extends BasePage {

    private static final By FIRST_NAME = testId("firstName");
    private static final By LAST_NAME = testId("lastName");
    private static final By POSTAL_CODE = testId("postalCode");
    private static final By CONTINUE_BUTTON = testId("continue");
    private static final By ERROR_MESSAGE = testId("error");

    @Override
    protected By pageAnchor() {
        return FIRST_NAME;
    }

    public CheckoutOverviewPage continueWith(CustomerInfo customer) {
        fill(customer);
        click(CONTINUE_BUTTON);
        return new CheckoutOverviewPage().waitUntilLoaded();
    }

    public CheckoutInformationPage continueExpectingError(CustomerInfo customer) {
        fill(customer);
        click(CONTINUE_BUTTON);
        waitVisible(ERROR_MESSAGE);
        return this;
    }

    public String getErrorMessage() {
        return getText(ERROR_MESSAGE);
    }

    private void fill(CustomerInfo customer) {
        type(FIRST_NAME, customer.firstName());
        type(LAST_NAME, customer.lastName());
        type(POSTAL_CODE, customer.postalCode());
    }
}
