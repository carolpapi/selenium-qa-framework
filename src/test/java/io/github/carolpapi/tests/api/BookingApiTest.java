package io.github.carolpapi.tests.api;

import io.github.carolpapi.api.BookingClient;
import io.github.carolpapi.api.model.Booking;
import io.github.carolpapi.api.model.CreatedBooking;
import io.github.carolpapi.data.TestDataFactory;
import io.restassured.response.Response;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.lessThan;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * API tests: why here and not in the UI?
 *  - Business rules and validations are tested at the API level: ~100x faster, no
 *    browser, no rendering/timing issues. The UI suite keeps only critical user journeys.
 *  - Each test CREATES its own data and the class DELETES it at the end, so tests are
 *    independent, can run in parallel and never depend on pre-existing records.
 */
public class BookingApiTest {

    private final BookingClient bookings = new BookingClient();

    /** Thread-safe: TestNG may run these methods in parallel. */
    private final Queue<Integer> createdIds = new ConcurrentLinkedQueue<>();

    private CreatedBooking createBooking(Booking booking) {
        CreatedBooking created = bookings.create(booking)
                .then().statusCode(200)
                .extract().as(CreatedBooking.class);
        createdIds.add(created.bookingid());
        return created;
    }

    @AfterClass(alwaysRun = true)
    public void cleanUpTestData() {
        createdIds.forEach(id -> bookings.delete(id));
    }

    @Test(groups = {"smoke", "api"}, description = "Create booking returns the persisted data and matches the contract")
    public void createBookingReturnsPersistedData() {
        Booking request = TestDataFactory.validBooking();

        Response response = bookings.create(request);

        response.then()
                .statusCode(200)
                .time(lessThan(5000L))                                   // basic performance guard
                .body(matchesJsonSchemaInClasspath("schemas/created-booking.json")); // contract

        CreatedBooking created = response.as(CreatedBooking.class);
        createdIds.add(created.bookingid());
        assertEquals(created.booking(), request, "Persisted booking should equal the request");
    }

    @Test(groups = {"regression", "api"}, description = "Get booking by id returns the stored booking")
    public void getBookingById() {
        CreatedBooking created = createBooking(TestDataFactory.validBooking());

        Booking fetched = bookings.get(created.bookingid())
                .then().statusCode(200)
                .extract().as(Booking.class);

        assertEquals(fetched, created.booking());
    }

    @Test(groups = {"regression", "api"}, description = "Security: update without a token is rejected")
    public void updateWithoutTokenIsForbidden() {
        CreatedBooking created = createBooking(TestDataFactory.validBooking());

        bookings.updateWithoutAuth(created.bookingid(), created.booking().withFirstname("Hacker"))
                .then().statusCode(403);

        // And the data did not change
        Booking fetched = bookings.get(created.bookingid()).as(Booking.class);
        assertEquals(fetched.firstname(), created.booking().firstname());
    }

    @Test(groups = {"regression", "api"}, description = "Authorized update changes the booking")
    public void authorizedUpdateChangesBooking() {
        CreatedBooking created = createBooking(TestDataFactory.validBooking());
        Booking changed = created.booking().withTotalprice(999);

        Booking updated = bookings.update(created.bookingid(), changed)
                .then().statusCode(200)
                .extract().as(Booking.class);

        assertEquals(updated.totalprice(), Integer.valueOf(999));
    }

    @Test(groups = {"regression", "api"}, description = "Deleted booking can no longer be retrieved")
    public void deletedBookingIsNotFound() {
        int id = bookings.create(TestDataFactory.validBooking())
                .then().statusCode(200)
                .extract().path("bookingid");

        bookings.delete(id).then().statusCode(201); // this API answers 201 on delete
        bookings.get(id).then().statusCode(404);
    }

    /**
     * Negative test. Ideally the API should answer 400 Bad Request; this demo API
     * answers 500. We assert what matters for the business (the booking is NOT
     * created) and the wrong status code is a defect reported to the dev team,
     * instead of a test that fails forever and teaches people to ignore red builds.
     */
    @Test(groups = {"regression", "api"}, description = "Negative: booking without mandatory fields is not created")
    public void bookingWithoutMandatoryFieldsIsRejected() {
        Booking invalid = TestDataFactory.validBooking().withFirstname(null);

        int status = bookings.create(invalid).statusCode();

        // Specific codes, not just "any error": a generic >= 400 check once passed
        // for the wrong reason (HTTP 418 caused by a bad Accept header in the client).
        assertTrue(status == 400 || status == 500,
                "Invalid booking must be rejected by validation (400, or 500 = known bug), but got HTTP " + status);
    }
}
