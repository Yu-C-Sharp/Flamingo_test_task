package api.helpers;

import api.models.Booking;
import api.utils.enums.FailedStatusCodes;
import api.utils.enums.PathParams;
import api.utils.enums.SuccessStatusCodes;
import api.utils.interfaces.IPath;
import config.TestConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.helpers.ApiSender.graphQlPost;
import static api.helpers.BodyBuilder.buildAuthRequest;
import static api.helpers.BodyBuilder.buildBooking;
import static api.utils.enums.Keys.BOOKING_ID;
import static api.utils.enums.Paths.*;
import static io.restassured.RestAssured.given;

public class ApiHelper {

    private static String buildEndpoint(String baseUrl, IPath path) {
        return baseUrl + path.getPath();
    }

    static RequestSpecification bookerSpec() {
        return given()
                .baseUri(TestConfig.BOOKER_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    static RequestSpecification graphqlSpec() {
        return given()
                .baseUri(TestConfig.GRAPHQL_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }

    public static Response getAuthResponse(String userName, String password) {
        final String body = buildAuthRequest(userName, password);
        final String endpoint = buildEndpoint(TestConfig.BOOKER_BASE_URL, AUTH);

        return ApiSender.apiPost(bookerSpec(), body, endpoint);
    }

    public static Response createBookingAndGetResponse(Booking booking) {
        return createBooking(booking);
    }

    public static int createBookingAndGetId(Booking booking) {
        return createBooking(booking).jsonPath().getInt(BOOKING_ID.getKey());
    }

    public static Response getBookingById(int bookingId) {
        final String endpoint = buildEndpoint(TestConfig.BOOKER_BASE_URL, BOOKING_BY_ID);
        Map<String, Integer> params = new HashMap<>();
        params.put(PathParams.ID.getParam(), bookingId);

        return ApiSender.getWithPathParams(bookerSpec(), params, endpoint);
    }

    public static List<Integer> getBookingIds() {
        Response response = getBookingList();
        if (response.statusCode() == SuccessStatusCodes.OK.getStatusCode()) {
            return response.jsonPath()
                    .getList(BOOKING_ID.getKey());
        }
        throw new AssertionError("createBooking expected 200 but got "
                + response.statusCode() + ": " + response.body().asString());
    }

    public static List<Integer> getBookingIdsByQueryParams(Map<String, ?> queryParams) {
        Response response = getBookingListByQueryParams(queryParams);
        if (response.statusCode() == SuccessStatusCodes.OK.getStatusCode()) {
            return response.jsonPath()
                    .getList(BOOKING_ID.getKey());
        }
        throw new AssertionError("createBooking expected 200 but got "
                + response.statusCode() + ": " + response.body().asString());
    }

    public static Response updateBooking(Booking updatedBooking, int bookingId, String... authToken) {
        final String body = buildBooking(updatedBooking);
        final String endpoint = buildEndpoint(TestConfig.BOOKER_BASE_URL, BOOKING_BY_ID);
        Map<String, Integer> params = new HashMap<>();
        params.put(PathParams.ID.getParam(), bookingId);

        return ApiSender.put(bookerSpec(), body, params, endpoint, authToken[0]);
    }

    public static Response deleteBooking(String authToken, int bookingId) {
        final String endpoint = buildEndpoint(TestConfig.BOOKER_BASE_URL, BOOKING_BY_ID);
        Map<String, Integer> params = new HashMap<>();
        params.put(PathParams.ID.getParam(), bookingId);

        return ApiSender.delete(bookerSpec(), authToken, params, endpoint);
    }

    public static Response graphPost(String body) {
        return graphQlPost(graphqlSpec(), body);
    }

    private static Response createBooking(Booking booking) {
        final String body = buildBooking(booking);
        final String endpoint = buildEndpoint(TestConfig.BOOKER_BASE_URL, BOOKING);

        int attemptCount = 3;
        do {
            Response response = ApiSender.apiPost(bookerSpec(), body, endpoint);

            if (response.statusCode() == SuccessStatusCodes.OK.getStatusCode()) {
                return response;
            }

            if (response.statusCode() != FailedStatusCodes.I_AM_A_TEAPOT.getStatusCode()) {
                throw new AssertionError("сreateBooking expected 200 but got "
                        + response.statusCode() + ": " + response.body().asString());
            }
            --attemptCount;
        } while (attemptCount > 1);

        throw new AssertionError("Unreachable");
    }

    private static Response getBookingList() {
        final String endpoint = buildEndpoint(TestConfig.BOOKER_BASE_URL, BOOKING);

        return ApiSender.get(bookerSpec(), endpoint);
    }

    private static Response getBookingListByQueryParams(Map<String, ?> queryParams) {
        final String endpoint = buildEndpoint(TestConfig.BOOKER_BASE_URL, BOOKING);

        return ApiSender.getWithQueryParams(bookerSpec(), queryParams, endpoint);
    }
}
