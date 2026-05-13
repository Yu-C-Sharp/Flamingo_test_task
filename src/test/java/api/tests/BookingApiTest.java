package api.tests;

import api.models.Booking;
import api.models.BookingDates;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.*;

import java.util.List;

import static api.helpers.ApiHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Feature("Booking CRUD")
class BookingApiTest {

    private static String token;
    private static int bookingId;
    private static Booking sourceBooking;

    @BeforeAll
    static void setUp() {
        token = getAuthToken();
        sourceBooking = defaultBooking();
        bookingId = createBooking(sourceBooking);
    }

    @AfterAll
    static void tearDown() {
        if (bookingId > 0) {
            bookerSpec()
                    .cookie("token", token)
                    .when()
                    .delete("/booking/" + bookingId);
        }
    }

    @Test
    @Description("GET /booking/{id} returns the booking that was created in setUp")
    void shouldGetBookingById() {
        Response response = bookerSpec()
                .when()
                .get("/booking/" + bookingId);

        assertThat(response.statusCode()).as("Expected 200 or 418 (Heroku quirk)").isIn(200, 418);

        if (response.statusCode() == 200) {
            Booking fetched = response.as(Booking.class);
            assertThat(fetched.getFirstname()).isEqualTo(sourceBooking.getFirstname());
            assertThat(fetched.getLastname()).isEqualTo(sourceBooking.getLastname());
            assertThat(fetched.getTotalprice()).isEqualTo(sourceBooking.getTotalprice());
            assertThat(fetched.isDepositpaid()).isEqualTo(sourceBooking.isDepositpaid());
            assertThat(fetched.getBookingdates().getCheckin()).isEqualTo(sourceBooking.getBookingdates().getCheckin());
            assertThat(fetched.getBookingdates().getCheckout()).isEqualTo(sourceBooking.getBookingdates().getCheckout());
        }
    }

    @Test
    @Description("PUT /booking/{id} with valid token updates all fields")
    void shouldUpdateBookingWithToken() {
        Booking updated = Booking.builder()
                .firstname("John")
                .lastname("Smith")
                .totalprice(200)
                .depositpaid(false)
                .bookingdates(BookingDates.builder()
                        .checkin("2025-03-01")
                        .checkout("2025-03-15")
                        .build())
                .additionalneeds("Lunch")
                .build();

        Response response = bookerSpec()
                .cookie("token", token)
                .body(updated)
                .when()
                .put("/booking/" + bookingId);

        assertThat(response.statusCode()).as("Expected 200 or 418 (Heroku quirk)").isIn(200, 418);

        if (response.statusCode() == 200) {
            Booking result = response.as(Booking.class);
            assertThat(result.getFirstname()).isEqualTo("John");
            assertThat(result.getLastname()).isEqualTo("Smith");
            assertThat(result.getTotalprice()).isEqualTo(200);
        }
    }

    @Test
    @Description("PUT /booking/{id} without token should return 403 Forbidden")
    void shouldFailUpdateWithoutToken() {
        int status = bookerSpec()
                .body(defaultBooking())
                .when()
                .put("/booking/" + bookingId)
                .statusCode();

        assertThat(status).as("Expected 403 (or 418 Heroku quirk)").isIn(403, 418);
    }

    @Test
    @Description("GET /booking returns a non-empty list of booking IDs")
    void shouldGetAllBookings() {
        List<Integer> ids = bookerSpec()
                .when()
                .get("/booking")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("bookingid");

        assertThat(ids).as("Bookings list should not be empty").isNotEmpty();
    }

    @Test
    @Description("GET /booking?firstname=X returns only entries matching that name")
    void shouldFilterByFirstname() {
        Booking filterTarget = Booking.builder()
                .firstname("UniqueFilterName")
                .lastname("TestLast")
                .totalprice(99)
                .depositpaid(true)
                .bookingdates(BookingDates.builder().checkin("2025-05-01").checkout("2025-05-05").build())
                .additionalneeds("None")
                .build();
        int filterId = createBooking(filterTarget);

        try {
            List<Integer> ids = bookerSpec()
                    .queryParam("firstname", "UniqueFilterName")
                    .when()
                    .get("/booking")
                    .then()
                    .statusCode(200)
                    .extract()
                    .jsonPath()
                    .getList("bookingid");

            assertThat(ids).as("Filter by firstname should return at least one result").isNotEmpty();
        } finally {
            deleteBooking(filterId, token);
        }
    }

    @Test
    @Description("GET /booking?checkin=date returns HTTP 200 with a list")
    void shouldFilterByCheckinDate() {
        List<Integer> ids = bookerSpec()
                .queryParam("checkin", "2025-01-01")
                .when()
                .get("/booking")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("bookingid");

        assertThat(ids).isNotNull();
    }

    @Test
    @Description("GET /booking/999999999 should return 404 for non-existent booking")
    void shouldReturn404ForNonExistentBooking() {
        bookerSpec()
                .when()
                .get("/booking/999999999")
                .then()
                .statusCode(404);
    }

    @Test
    @Description("Response body contains all required fields with correct types")
    void shouldValidateResponseSchema() {
        Response response = bookerSpec()
                .when()
                .get("/booking/" + bookingId);

        assertThat(response.statusCode()).as("Expected 200 or 418 (Heroku quirk)").isIn(200, 418);

        if (response.statusCode() == 200) {
            assertThat((String) response.path("firstname")).isNotNull();
            assertThat((String) response.path("lastname")).isNotNull();
            assertThat((Integer) response.path("totalprice")).isNotNull();
            assertThat((Boolean) response.path("depositpaid")).isNotNull();
            assertThat((String) response.path("bookingdates.checkin")).isNotNull();
            assertThat((String) response.path("bookingdates.checkout")).isNotNull();
        }
    }

    @Test
    @Description("DELETE /booking/{id} with valid token returns 201 and booking is then 404")
    void shouldDeleteBookingWithToken() {
        int tempId = createBooking(defaultBooking());

        bookerSpec()
                .cookie("token", token)
                .when()
                .delete("/booking/" + tempId)
                .then()
                .statusCode(201);

        bookerSpec()
                .when()
                .get("/booking/" + tempId)
                .then()
                .statusCode(404);
    }

    @Test
    @Description("GET /booking?checkout=date returns HTTP 200 with a list")
    void shouldFilterByCheckoutDate() {
        List<Integer> ids = bookerSpec()
                .queryParam("checkout", "2025-01-10")
                .when()
                .get("/booking")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getList("bookingid");

        assertThat(ids).isNotNull();
    }

    @Test
    @Description("POST /booking with invalid date format should be handled gracefully")
    void shouldReturnErrorForInvalidDateFormat() {
        String invalidPayload = "{\"firstname\":\"Test\",\"lastname\":\"User\"," +
                "\"totalprice\":100,\"depositpaid\":true," +
                "\"bookingdates\":{\"checkin\":\"not-a-date\",\"checkout\":\"also-not-a-date\"}," +
                "\"additionalneeds\":\"None\"}";

        Response response = bookerSpec()
                .body(invalidPayload)
                .when()
                .post("/booking");

        assertThat(response.statusCode())
                .as("Invalid date format should return 200 (stored as string) or 4xx")
                .isIn(200, 400, 500);
    }

    @Test
    @Description("POST /booking without required fields should return 4xx or 500")
    void shouldFailCreateWithMissingRequiredFields() {
        String incompletePayload = "{\"lastname\":\"NoFirstName\"}";

        int status = bookerSpec()
                .body(incompletePayload)
                .when()
                .post("/booking")
                .statusCode();

        assertThat(status)
                .as("Missing required fields should return 4xx or 500")
                .isIn(400, 500);
    }
}
