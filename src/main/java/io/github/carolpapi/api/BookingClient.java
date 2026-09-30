package io.github.carolpapi.api;

import io.github.carolpapi.api.model.Booking;
import io.restassured.response.Response;

/**
 * Business operations on /booking. Returns the raw Response so each test decides
 * what to assert (status, body, headers, schema, response time).
 */
public class BookingClient extends ApiClient {

    private final AuthClient auth = new AuthClient();

    public Response create(Booking booking) {
        return request().body(booking).post("/booking");
    }

    public Response get(int bookingId) {
        return request().get("/booking/{id}", bookingId);
    }

    public Response update(int bookingId, Booking booking) {
        return request().cookie("token", auth.token()).body(booking).put("/booking/{id}", bookingId);
    }

    /** Used by negative tests: same call, no credentials. */
    public Response updateWithoutAuth(int bookingId, Booking booking) {
        return request().body(booking).put("/booking/{id}", bookingId);
    }

    public Response delete(int bookingId) {
        return request().cookie("token", auth.token()).delete("/booking/{id}", bookingId);
    }
}
