package io.github.carolpapi.pages;

import io.github.carolpapi.core.BasePage;
import org.openqa.selenium.By;

public class CheckoutCompletePage extends BasePage {

    private static final By COMPLETE_HEADER = testId("complete-header");

    @Override
    protected By pageAnchor() {
        return COMPLETE_HEADER;
    }

    public String getConfirmationMessage() {
        return getText(COMPLETE_HEADER);
    }
}
