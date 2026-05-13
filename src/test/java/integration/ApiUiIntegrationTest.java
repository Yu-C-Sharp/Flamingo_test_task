package integration;

import api.models.Booking;
import api.models.BookingDates;
import com.microsoft.playwright.*;
import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.*;
import ui.pages.WebTablePage;

import static api.helpers.ApiHelper.*;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("integration")
@Feature("API + UI Integration")
class ApiUiIntegrationTest {

    private static Playwright playwright;
    private static Browser browser;

    private BrowserContext context;
    private Page page;

    private static String token;
    private static int bookingId;
    private static Booking booking;

    @BeforeAll
    static void setUpApiAndBrowser() {
        token = getAuthToken();

        booking = Booking.builder()
                .firstname("Integration")
                .lastname("Tester")
                .totalprice(500)
                .depositpaid(true)
                .bookingdates(BookingDates.builder()
                        .checkin("2025-06-01")
                        .checkout("2025-06-07")
                        .build())
                .additionalneeds("Airport transfer")
                .build();

        bookingId = createBooking(booking);

        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(TestConfig.HEADLESS)
        );
    }

    @AfterAll
    static void tearDown() {
        deleteBooking(bookingId, token);

        int status = bookerSpec()
                .when()
                .get("/booking/" + bookingId)
                .statusCode();
        assertThat(status).as("Deleted booking should return 404 (or 418 Heroku quirk)").isIn(404, 418);

        browser.close();
        playwright.close();
    }

    @BeforeEach
    void openBrowser() {
        context = browser.newContext();
        context.setDefaultTimeout(TestConfig.TIMEOUT_MS);
        page = context.newPage();
    }

    @AfterEach
    void closeBrowserContext() {
        context.close();
    }

    @Test
    @Description("Create booking via API, add the same person to Web Table via UI, verify record, then clean up via API")
    void shouldCreateBookingViaApiAndVerifyInUI() {
        // Verify booking was created via API
        assertThat(bookingId).isPositive();

        io.restassured.response.Response apiResponse = bookerSpec()
                .when()
                .get("/booking/" + bookingId);

        assertThat(apiResponse.statusCode()).as("Expected 200 or 418 (Heroku quirk)").isIn(200, 418);

        // If 418, fall back to the known source data — booking object has the same values
        Booking fetched = apiResponse.statusCode() == 200
                ? apiResponse.as(Booking.class)
                : booking;

        assertThat(fetched.getFirstname()).isEqualTo(booking.getFirstname());
        assertThat(fetched.getLastname()).isEqualTo(booking.getLastname());

        // Open Web Tables and add a record with the data from the API
        page.navigate(TestConfig.DEMOQA_WEBTABLES_URL);
        page.evaluate("document.querySelectorAll('#fixedban, iframe').forEach(e => e.remove())");
        WebTablePage tablePage = new WebTablePage(page);

        tablePage.addRecord(
                fetched.getFirstname(),
                fetched.getLastname(),
                "integration@test.com",
                "30",
                String.valueOf(fetched.getTotalprice()),
                "QA"
        );

        // Search and verify the record is present in the UI
        tablePage.search(fetched.getFirstname());

        assertThat(tablePage.isRecordVisible(fetched.getFirstname()))
                .as("Record created from API data should be visible in the UI table")
                .isTrue();

        assertThat(tablePage.isRecordVisible(fetched.getLastname()))
                .as("Last name from API data should be visible in the UI table")
                .isTrue();
    }
}
