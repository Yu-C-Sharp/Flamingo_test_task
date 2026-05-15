package api.tests;

import api.models.auth.AuthResponse;
import config.TestConfig;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static api.helpers.ApiHelper.getAuthResponse;
import static api.utils.enums.FailedStatusCodes.I_AM_A_TEAPOT;
import static api.utils.enums.SuccessStatusCodes.OK;
import static org.assertj.core.api.Assertions.assertThat;

@Tag("api")
@Feature("Authentication")
class AuthenticationTest {

    @Test
    @Description("Valid credentials should return an auth token")
    void authWithValidCredentials() {
        Response response = getAuthResponse(TestConfig.BOOKER_USERNAME, TestConfig.BOOKER_PASSWORD);

        assertThat(response.statusCode() ).as("Status code doesn't match to expected")
                .isIn(List.of(OK.getStatusCode(), I_AM_A_TEAPOT.getStatusCode()));

        assertThat(response.as(AuthResponse.class).getToken())
                .as("Auth token should not be null or empty")
                .isNotNull()
                .isNotEmpty();
    }

    @ParameterizedTest
    @CsvFileSource(resources = "/testcredentials.csv", numLinesToSkip = 1)
    @Description("Invalid password should return 'Bad credentials'")
    void authWithInvalidCredentials(String userName, String password) {
        var response = getAuthResponse(userName, password).as(AuthResponse.class);

        assertThat(response.getReason())
                .as("Response reason should indicate bad credentials")
                .isEqualTo("Bad credentials");
        assertThat(response.getToken()).isNull();
    }
}
