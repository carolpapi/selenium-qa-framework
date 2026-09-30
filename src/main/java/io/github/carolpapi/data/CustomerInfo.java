package io.github.carolpapi.data;

/** Immutable test data for the checkout form. Built by {@link TestDataFactory}. */
public record CustomerInfo(String firstName, String lastName, String postalCode) {

    public CustomerInfo withPostalCode(String postalCode) {
        return new CustomerInfo(firstName, lastName, postalCode);
    }

    public CustomerInfo withFirstName(String firstName) {
        return new CustomerInfo(firstName, lastName, postalCode);
    }
}
