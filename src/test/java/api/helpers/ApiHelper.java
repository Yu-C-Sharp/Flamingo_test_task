package api.helpers;

import api.models.AuthResponse;
import api.models.Booking;
import api.models.BookingDates;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import config.TestConfig;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static io.restassured.RestAssured.given;

public class ApiHelper {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static RequestSpecification bookerSpec() {
        return given()
                .baseUri(TestConfig.BOOKER_BASE_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .header("Connection", "close");
    }

    public static String getAuthToken() {
        return bookerSpec()
                .body(String.format("{\"username\":\"%s\",\"password\":\"%s\"}",
                        TestConfig.BOOKER_USERNAME, TestConfig.BOOKER_PASSWORD))
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class)
                .getToken();
    }

    public static Booking defaultBooking() {
        return Booking.builder()
                .firstname("James")
                .lastname("Brown")
                .totalprice(150)
                .depositpaid(true)
                .bookingdates(BookingDates.builder()
                        .checkin("2025-01-01")
                        .checkout("2025-01-10")
                        .build())
                .additionalneeds("Breakfast")
                .build();
    }

    // Uses Java's built-in HttpClient because Apache HttpClient (REST Assured) occasionally
    // returns 418 from Restful Booker's Heroku server due to stale keep-alive connections on POST.
    public static int createBooking(Booking booking) {
        try {
            String json = MAPPER.writeValueAsString(booking);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(TestConfig.BOOKER_BASE_URL + "/booking"))
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> response = HttpClient.newHttpClient()
                    .send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode node = MAPPER.readTree(response.body());
                return node.get("bookingid").asInt();
            }
            throw new AssertionError(
                    "createBooking expected 200 but got " + response.statusCode() + ": " + response.body());
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("createBooking failed", e);
        }
    }

    public static void deleteBooking(int bookingId, String token) {
        bookerSpec()
                .cookie("token", token)
                .when()
                .delete("/booking/" + bookingId)
                .then()
                .statusCode(201);
    }

    public static RequestSpecification graphqlSpec() {
        return given()
                .baseUri(TestConfig.GRAPHQL_URL)
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON);
    }
}
