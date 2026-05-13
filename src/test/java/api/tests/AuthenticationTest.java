package api.tests;

import api.models.AuthResponse;
import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static api.helpers.ApiHelper.bookerSpec;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Feature("Authentication")
class AuthenticationTest {

    @Test
    @Description("Valid credentials should return an auth token")
    void shouldReturnTokenOnValidCredentials() {
        AuthResponse response = bookerSpec()
                .body("{\"username\":\"" + TestConfig.BOOKER_USERNAME + "\",\"password\":\"" + TestConfig.BOOKER_PASSWORD + "\"}")
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class);

        assertThat(response.getToken())
                .as("Auth token should not be null or empty")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @Description("Invalid password should return Bad credentials")
    void shouldReturnBadCredentialsOnInvalidPassword() {
        AuthResponse response = bookerSpec()
                .body("{\"username\":\"admin\",\"password\":\"wrongpassword\"}")
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class);

        assertThat(response.getReason())
                .as("Response reason should indicate bad credentials")
                .isEqualTo("Bad credentials");
        assertThat(response.getToken()).isNull();
    }

    @Test
    @Description("Invalid username should return Bad credentials")
    void shouldReturnBadCredentialsOnInvalidUsername() {
        AuthResponse response = bookerSpec()
                .body("{\"username\":\"nobody\",\"password\":\"password123\"}")
                .when()
                .post("/auth")
                .then()
                .statusCode(200)
                .extract()
                .as(AuthResponse.class);

        assertThat(response.getReason())
                .as("Response reason should indicate bad credentials")
                .isEqualTo("Bad credentials");
        assertThat(response.getToken()).isNull();
    }
}
