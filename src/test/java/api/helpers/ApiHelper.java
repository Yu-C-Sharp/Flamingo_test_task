package api.helpers;

import api.models.booking.Booking;
import api.utils.enums.PathParams;
import api.utils.enums.SuccessStatusCodes;
import api.utils.interfaces.IPath;
import api.utils.interfaces.IStatusCode;
import config.TestConfig;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.extern.java.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static api.helpers.ApiSender.graphQlPost;
import static api.helpers.BodyBuilder.buildAuthRequest;
import static api.helpers.BodyBuilder.buildBooking;
import static api.utils.enums.Keys.BOOKING_ID;
import static api.utils.enums.ApiPaths.*;
import static io.restassured.RestAssured.given;

import io.qameta.allure.restassured.AllureRestAssured;

@Log
public class ApiHelper {

    static RequestSpecification bookerSpec() {
        return given()
                .baseUri(TestConfig.BOOKER_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Connection", "close")
                .filter(new AllureRestAssured());
    }

    static RequestSpecification graphqlSpec() {
        return given()
                .baseUri(TestConfig.GRAPHQL_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new AllureRestAssured());
    }

    static RequestSpecification bookStoreSpec() {
        return given()
                .baseUri(TestConfig.DEMOQA_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .filter(new AllureRestAssured());
    }

    public static Response getBookStoreBooks() {
        return ApiSender.get(bookStoreSpec(), TestConfig.DEMOQA_BASE_URL + BOOK_STORE_BOOKS.getPath());
    }

    public static Response getAuthResponse(String userName, String password) {
        final String body = buildAuthRequest(userName, password);
        final String endpoint = buildApiEndpoint(AUTH);

        return ApiSender.apiPost(bookerSpec(), body, endpoint);
    }

    public static Response createBookingAndGetResponse(Booking booking) {
        final String body = buildBooking(booking);
        final String endpoint = buildApiEndpoint(BOOKING);
        logger.info("Sending raw POST /booking (no retry wrapper) for negative-scenario test");

        return ApiSender.apiPost(bookerSpec(), body, endpoint);
    }

    public static int createBookingAndGetId(Booking booking) {
        int id = createBooking(booking).jsonPath().getInt(BOOKING_ID.getKey());
        logger.info("Booking created with ID: " + id);

        return id;
    }

    public static Response getBookingById(int bookingId) {
        final String endpoint = buildApiEndpoint(BOOKING_BY_ID);
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
        final String endpoint = buildApiEndpoint(BOOKING_BY_ID);
        Map<String, Integer> params = new HashMap<>();
        params.put(PathParams.ID.getParam(), bookingId);

        String token = authToken.length > 0 ? authToken[0] : null;
        logger.info("Updating booking ID=" + bookingId + (token != null ? " with token" : " WITHOUT token"));
        return ApiSender.put(bookerSpec(), body, params, endpoint, token);
    }

    public static Response deleteBooking(String authToken, int bookingId) {
        final String endpoint = buildApiEndpoint(BOOKING_BY_ID);
        Map<String, Integer> params = new HashMap<>();
        params.put(PathParams.ID.getParam(), bookingId);

        return ApiSender.delete(bookerSpec(), authToken, params, endpoint);
    }

    public static Response graphPostRequest(String body, IStatusCode expectedStatusCode) {
        return graphQlPost(graphqlSpec(), body)
                .then()
                .statusCode(expectedStatusCode.getStatusCode())
                .extract()
                .response();
    }

    private static String buildApiEndpoint(IPath path) {
        return TestConfig.BOOKER_BASE_URL + path.getPath();
    }

    private static Response createBooking(Booking booking) {
        final String body = buildBooking(booking);
        final String endpoint = buildApiEndpoint(BOOKING);

        return ApiSender.apiPost(bookerSpec(), body, endpoint)
                .then()
                .statusCode(SuccessStatusCodes.OK.getStatusCode())
                .extract()
                .response();
    }

    private static Response getBookingList() {
        final String endpoint = buildApiEndpoint(BOOKING);

        return ApiSender.get(bookerSpec(), endpoint);
    }

    private static Response getBookingListByQueryParams(Map<String, ?> queryParams) {
        final String endpoint = buildApiEndpoint(BOOKING);

        return ApiSender.getWithQueryParams(bookerSpec(), queryParams, endpoint);
    }
}
