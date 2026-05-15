package api.tests;

import api.models.Booking;
import api.models.BookingDates;
import api.models.auth.AuthResponse;
import api.utils.enums.FailedStatusCodes;
import api.utils.enums.SuccessStatusCodes;
import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import lombok.extern.java.Log;
import org.junit.jupiter.api.*;
import utils.enums.DatePatterns;

import java.util.*;

import static api.helpers.ApiHelper.*;
import static org.assertj.core.api.Assertions.assertThat;
import static utils.constants.CommonConstants.faker;
import static utils.helpers.DateHelper.addDaysToLocalDate;
import static utils.helpers.DateHelper.getToday;

@Log
@Tag("api")
@Feature("Booking CRUD")
class BookingApiTest {

    private final String TEAPOT_ERROR_MESSAGE = "'I am a teapot' - Is someone trying to have fun?";

    private static String token;

    private static Integer bookingId;

    private static Set<Integer> bookingIdSet;

    private static Booking sourceBooking;

    @BeforeAll
    static void setUp() {
        token = getAuthResponse(TestConfig.BOOKER_USERNAME, TestConfig.BOOKER_PASSWORD)
                .as(AuthResponse.class)
                .getToken();
        bookingIdSet = new HashSet<>();
    }

    @AfterAll
    static void tearDown() {
        if (!bookingIdSet.isEmpty()) {
            bookingIdSet.forEach(id -> deleteBooking(token, id));
        }
    }

    @Test
    @Description("POST /booking returns a valid ID")
    void createBookingAndCheckId() {
        createBooking();

        assertThat(bookingId).as("Booking ID was not returned")
                .isNotNull()
                .as("Booking ID must be positive")
                .isPositive();

        bookingIdSet.add(bookingId);
    }

    @Test
    @Description("GET /booking/{id} returns the booking that was created in set up")
    void shouldGetBookingById() {
        createBooking();

        Response response = getBookingById(bookingId);
        checkOkStatusCode(response);

        if (response.getStatusCode() == SuccessStatusCodes.OK.getStatusCode()) {
            Booking fetched = response.as(Booking.class);
            assertThat(fetched.getFirstname()).isEqualTo(sourceBooking.getFirstname());
            assertThat(fetched.getLastname()).isEqualTo(sourceBooking.getLastname());
            assertThat(fetched.getTotalprice()).isEqualTo(sourceBooking.getTotalprice());
            assertThat(fetched.isDepositpaid()).isEqualTo(sourceBooking.isDepositpaid());
            assertThat(fetched.getBookingdates().getCheckin()).isEqualTo(sourceBooking.getBookingdates().getCheckin());
            assertThat(fetched.getBookingdates().getCheckout()).isEqualTo(sourceBooking.getBookingdates().getCheckout());
            assertThat(fetched.getAdditionalneeds()).isEqualTo(sourceBooking.getAdditionalneeds());
        } else {
            logger.info(TEAPOT_ERROR_MESSAGE);
        }
    }

    @Test
    @Description("PUT /booking/{id} with valid token updates all fields")
    void shouldUpdateBookingWithToken() {
        createBooking();
        Booking updated = getBookingModel();

        Response response = updateBooking(updated, bookingId, token);

        checkOkStatusCode(response);

        if (response.getStatusCode() == SuccessStatusCodes.OK.getStatusCode()) {
            Booking result = response.as(Booking.class);
            assertThat(result.getFirstname()).isEqualTo(updated.getFirstname());
            assertThat(result.getLastname()).isEqualTo(updated.getLastname());
            assertThat(result.getTotalprice()).isEqualTo(updated.getTotalprice());
            assertThat(result.isDepositpaid()).isEqualTo(updated.isDepositpaid());
            assertThat(result.getBookingdates().getCheckin()).isEqualTo(updated.getBookingdates().getCheckin());
            assertThat(result.getBookingdates().getCheckout()).isEqualTo(updated.getBookingdates().getCheckout());
            assertThat(result.getAdditionalneeds()).isEqualTo(updated.getAdditionalneeds());
        } else {
            logger.info(TEAPOT_ERROR_MESSAGE);
        }
    }

    @Test
    @Description("PUT /booking/{id} without token should return 403 Forbidden")
    void shouldFailUpdateWithoutToken() {
        createBooking();
        int statusCode = updateBooking(getBookingModel(), bookingId).getStatusCode();

        assertThat(statusCode).as("Expected 403 (or 418 Heroku quirk)")
                .isIn(FailedStatusCodes.FORBIDDEN.getStatusCode(), FailedStatusCodes.I_AM_A_TEAPOT.getStatusCode());
    }

    @Test
    @Description("GET /booking returns a non-empty list of booking IDs")
    void shouldGetAllBookings() {
        createBooking();
        List<Integer> ids = getBookingIds();

        assertThat(ids).as("Bookings list should not be empty").isNotEmpty();
        assertThat(ids).as("Booking IDs list should contain the created ID")
                .contains(bookingId);

    }

    @Test
    @Description("GET /booking?firstname=X returns only entries matching that name")
    void shouldFilterByFirstname() {
        final String uniqueFirstName = "UniqueFilterName";
        final String uniqueLastName = "UniqueFilterLastName";
        sourceBooking = getBookingModel();
        sourceBooking.setFirstname(uniqueFirstName);
        sourceBooking.setLastname(uniqueLastName);
        bookingId = createBookingAndGetId(sourceBooking);
        bookingIdSet.add(bookingId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("firstname", uniqueFirstName);
        queryParams.put("lastname", uniqueLastName);
        List<Integer> ids = getBookingIdsByQueryParams(queryParams);

        assertThat(ids).as("Filter by firstname should return at least one result")
                .isNotEmpty();
        assertThat(ids).as("Booking IDs query list should contain the created ID")
                .contains(bookingId);
    }

    @Test
    @Description("GET /booking?checkin=date returns HTTP 200 with a list")
    void shouldFilterByCheckInDate() {
        getBookingModel();
        final String uniqueCheckIn = "2027-12-31";
        final String uniqueCheckOut = "2028-01-31";
        sourceBooking.setBookingdates(new BookingDates(uniqueCheckIn, uniqueCheckOut));
        bookingId = createBookingAndGetId(sourceBooking);
        bookingIdSet.add(bookingId);

        Map<String, String> queryParams = new HashMap<>();
        queryParams.put("checkin", uniqueCheckIn);
        queryParams.put("checkout", uniqueCheckOut);
        List<Integer> ids = getBookingIdsByQueryParams(queryParams);

        assertThat(ids).as("Filter by firstname should return at least one result").isNotEmpty();
        assertThat(ids).as("Booking IDs query list should contain the created ID")
                .contains(bookingId);
    }

    @Test
    @Description("GET /booking/999999999 should return 404 for non-existent booking")
    void shouldReturn404ForNonExistentBooking() {
        final int invalidBookingId = 999999999;
        assertThat(getBookingById(invalidBookingId).getStatusCode()).as("Status code doesn't match to expected")
                .isEqualTo(FailedStatusCodes.NOT_FOUND.getStatusCode());
    }

    @Test
    @Description("Response body contains all required fields with correct types")
    void shouldValidateResponseSchema() {
        createBooking();
        Response response = getBookingById(bookingId);
        checkOkStatusCode(response);

        if (response.getStatusCode() == SuccessStatusCodes.OK.getStatusCode()) {
            assertThat((String) response.path("firstname")).isNotNull();
            assertThat((String) response.path("lastname")).isNotNull();
            assertThat((Integer) response.path("totalprice")).isNotNull();
            assertThat((Boolean) response.path("depositpaid")).isNotNull();
            assertThat((String) response.path("bookingdates.checkin")).isNotNull();
            assertThat((String) response.path("bookingdates.checkout")).isNotNull();
            assertThat((String) response.path("additionalneeds")).isNotNull();
        } else {
            logger.info(TEAPOT_ERROR_MESSAGE);
        }
    }

    @Test
    @Description("DELETE /booking/{id} with valid token returns 201 and booking is then 404")
    void shouldDeleteBookingWithToken() {
        createBooking();

        assertThat(deleteBooking(token, bookingId).getStatusCode()).as("Expected 201 or 418 (Heroku quirk)")
                .isIn(SuccessStatusCodes.CREATED.getStatusCode(), FailedStatusCodes.I_AM_A_TEAPOT.getStatusCode());

        assertThat(getBookingById(bookingId).getStatusCode()).as("Expected 404 or 418 (Heroku quirk)")
                .isIn(FailedStatusCodes.NOT_FOUND.getStatusCode());
    }

    @Test
    @Description("POST /booking with invalid date format should be handled gracefully")
    void shouldReturnErrorForInvalidDateFormat() {
        getBookingModel();
        final String invalidCheckInDate = "not-a-date";
        final String invalidCheckOutDate = "also-not-a-date";
        sourceBooking.setBookingdates(new BookingDates(invalidCheckInDate, invalidCheckOutDate));

        Response response = createBookingAndGetResponse(sourceBooking);

        assertThat(response.getStatusCode())
                .as("Invalid date format should return 200 (stored as string) or 4xx")
                .isIn(FailedStatusCodes.BAD_REQUEST.getStatusCode(), FailedStatusCodes.INTERNAL_SERVER_ERROR.getStatusCode());
    }

    @Test
    @Description("POST /booking without required fields should return 4xx or 500")
    void shouldFailCreateWithMissingRequiredFields() {
        Booking incompleteBooking = new Booking();
        incompleteBooking.setLastname("NoFirstName");
        incompleteBooking.setBookingdates(new BookingDates());

        assertThat(createBookingAndGetResponse(incompleteBooking).getStatusCode())
                .as("Missing required fields should return 4xx or 500")
                .isIn(400, 500);
    }

    private static Booking getBookingModel() {
        final int daysCount = faker.random().nextInt(3, 10);
        final int totalPrice = faker.random().nextInt(100, 250);

        final boolean depositPaid = true;

        final String firstName = faker.funnyName().name();
        final String lastName = faker.name().lastName();
        final String checkInDate = getToday(DatePatterns.YEAR_MONTH_DAY.getPattern());
        final String checkOutDate = addDaysToLocalDate(DatePatterns.YEAR_MONTH_DAY.getPattern(), checkInDate, daysCount);
        final String additionalNeeds = "Dinner";

        return new Booking(
                firstName, lastName, totalPrice, depositPaid,
                new BookingDates(checkInDate, checkOutDate), additionalNeeds
        );
    }

    private static void createBooking() {
        sourceBooking = getBookingModel();
        bookingId = createBookingAndGetId(sourceBooking);
        bookingIdSet.add(bookingId);
    }

    private static void checkOkStatusCode(Response response) {
        assertThat(response.getStatusCode()).as("Expected 200 or 418 (Heroku quirk)")
                .isIn(SuccessStatusCodes.OK.getStatusCode(), FailedStatusCodes.I_AM_A_TEAPOT.getStatusCode());
    }
}
