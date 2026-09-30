package io.github.carolpapi.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of POST /booking: the generated id + the persisted booking. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CreatedBooking(int bookingid, Booking booking) {
}
