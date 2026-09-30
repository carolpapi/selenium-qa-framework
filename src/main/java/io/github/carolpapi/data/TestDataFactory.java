package io.github.carolpapi.data;

import io.github.carolpapi.api.model.Booking;
import io.github.carolpapi.api.model.BookingDates;
import net.datafaker.Faker;

import java.time.LocalDate;
import java.util.Locale;

/**
 * One place to create test data.
 *
 * - Every call generates UNIQUE data, so tests never depend on each other or on
 *   leftovers from a previous run (a top cause of flakiness in parallel/CI runs).
 * - Tests ask for "a valid customer" and override only the field the scenario
 *   is about: validCustomer().withPostalCode("") reads as the business rule.
 */
public final class TestDataFactory {

    private static final Faker FAKER = new Faker(Locale.US);

    private TestDataFactory() {
    }

    public static CustomerInfo validCustomer() {
        return new CustomerInfo(
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                FAKER.address().zipCode());
    }

    public static Booking validBooking() {
        LocalDate checkin = LocalDate.now().plusDays(FAKER.number().numberBetween(1, 60));
        LocalDate checkout = checkin.plusDays(FAKER.number().numberBetween(1, 14));
        return new Booking(
                FAKER.name().firstName(),
                FAKER.name().lastName(),
                FAKER.number().numberBetween(50, 2000),
                FAKER.bool().bool(),
                new BookingDates(checkin.toString(), checkout.toString()),
                FAKER.options().option("Breakfast", "Late checkout", "Extra bed", "None"));
    }
}
