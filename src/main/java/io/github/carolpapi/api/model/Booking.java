package io.github.carolpapi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Request/response body for /booking. Field names match the API contract
 * (lower case, as the API returns them), so Jackson maps them with no annotations.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record Booking(
        String firstname,
        String lastname,
        Integer totalprice,
        Boolean depositpaid,
        BookingDates bookingdates,
        String additionalneeds) {

    public Booking withFirstname(String firstname) {
        return new Booking(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }

    public Booking withTotalprice(Integer totalprice) {
        return new Booking(firstname, lastname, totalprice, depositpaid, bookingdates, additionalneeds);
    }
}
