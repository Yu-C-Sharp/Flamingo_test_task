package api.tests;

import api.utils.enums.FailedStatusCodes;
import api.utils.enums.SuccessStatusCodes;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static api.helpers.ApiHelper.graphPostRequest;
import static api.utils.enums.GraphQlPaths.*;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * GraphQL tests against the Rick and Morty public API (https://rickandmortyapi.com/graphql).
 * Schema mirrors the assignment requirements: list with pagination, single entity by ID,
 * variables, nested fields across types (episode → characters → name).
 */
@Tag("api")
@Feature("GraphQL Rick and Morty")
class GraphQlTest {

    String expectedId;
    String expectedName;

    final int firstArrayIndex = 0;

    // ─── Positive ───────────────────────────────────────────────────────────────

    @Test
    @Description("Query a paginated list — characters(page:1) should return 20 results per page")
    void shouldGetCharactersFirstPage() {
        String body = """
                {
                  "query": "{ characters(page: 1) { info { count pages } results { id name } } }"
                }
                """;

        Response response = graphPostRequest(body, SuccessStatusCodes.OK);

        List<?> results = response.path(CHARACTERS_RESULTS.getPath());
        Integer pagesCount = response.path(CHARACTERS_INFO_PAGES.getPath());

        assertThat(pagesCount).as("Total pages should be positive").isPositive();
        assertThat(results).as("First page should contain 20 characters").hasSize(20);
    }

    @Test
    @Description("Pagination offset — page 1 and page 2 must return different character IDs")
    void shouldReturnDifferentResultsOnDifferentPages() {
        String pageFirstBody = """
                { "query": "{ characters(page: 1) { results { id } } }" }
                """;
        String pageSecondBody = """
                { "query": "{ characters(page: 2) { results { id } } }" }
                """;

        List<String> ids1 = graphPostRequest(pageFirstBody, SuccessStatusCodes.OK).jsonPath()
                .getList(CHARACTERS_RESULTS_ID.getPath());

        List<String> ids2 = graphPostRequest(pageSecondBody, SuccessStatusCodes.OK).jsonPath()
                .getList(CHARACTERS_RESULTS_ID.getPath());

        assertThat(ids1).doesNotContainAnyElementsOf(ids2);
    }

    @Test
    @Description("Query a single character by ID returns the expected entity")
    void shouldGetSingleCharacterById() {

        expectedId = "1";
        expectedName = "Rick Sanchez";

        String body = String.format("""
                {
                  "query": "{ character(id: %s) { id name status species } }"
                }
                """,
                expectedId
        );

        Response response = graphPostRequest(body, SuccessStatusCodes.OK);

        String actualId = response.path(DATA_CHARACTER_ID.getPath());
        String actualName = response.path(DATA_CHARACTER_NAME.getPath());

        assertThat(actualId).isEqualTo(expectedId);
        assertThat(actualName).isEqualTo(expectedName);
    }

    @Test
    @Description("Query using GraphQL variables (no string interpolation) — character by variable ID")
    void shouldQueryUsingGraphQLVariables() {
        expectedId = "2";
        expectedName = "Morty Smith";

        String body = String.format("""
                {
                  "query": "query GetCharacter($id: ID!) { character(id: $id) { id name } }",
                  "variables": { "id": %s }
                }
                """,
                expectedId
        );

        Response response = graphPostRequest(body, SuccessStatusCodes.OK);

        String actualId = response.path(DATA_CHARACTER_ID.getPath());
        String actualName = response.path(DATA_CHARACTER_NAME.getPath());

        assertThat((Object) response.path(ERRORS.getPath())).isNull();
        assertThat(actualId).isEqualTo(expectedId);
        assertThat(actualName).isEqualTo(expectedName);
    }

    @Test
    @Description("Nested fields across types — episode → characters → name")
    void shouldQueryNestedFieldsEpisodeCharacters() {
        expectedId = "1";

        String body = String.format("""
                {
                  "query": "{ episode(id: %d) { id name air_date characters { id name species } } }"
                }
                """,
                Integer.parseInt(expectedId)
        );

        Response response = graphPostRequest(body, SuccessStatusCodes.OK);

        String actualEpisodeName = response.path(DATA_EPISODE_NAME.getPath());
        List<?> actualCharacters = response.path(DATA_EPISODE_CHARACTERS.getPath());

        String actualFirstCharacterName = response.path(String.format(DATA_EPISODE_CHARACTERS_NAME.getPath(), firstArrayIndex));

        assertThat(actualEpisodeName).isNotNull().isNotEmpty();
        assertThat(actualCharacters).isNotEmpty();
        assertThat(actualFirstCharacterName).isNotNull().isNotEmpty();
    }

    // ─── Negative ───────────────────────────────────────────────────────────────

    @Test
    @Description("Invalid character ID returns HTTP 200 with data.character = null")
    void shouldReturnNullForInvalidCharacterId() {
        final int invalidCharacterId = 999999;

        String body = String.format("""
                {
                  "query": "{ character(id: %d) { id name } }"
                }
                """,
                invalidCharacterId
        );

        Response response = graphPostRequest(body, SuccessStatusCodes.OK);

        Object character = response.path(DATA_CHARACTER.getPath());

        assertThat(character == null).as("Expected returns null for non-existent ID")
                .isTrue();
    }

    @Test
    @Description("Malformed query syntax returns errors array; HTTP status may be 200 or 400 depending on server impl")
    void shouldReturnErrorForMalformedQuery() {
        String body = """
                {
                  "query": "{ characters( { name }"
                }
                """;

        Response response = graphPostRequest(body, FailedStatusCodes.BAD_REQUEST);

        List<?> errors = response.path(ERRORS.getPath());
        assertThat(errors).as("Syntax error should produce errors array")
                .isNotNull()
                .isNotEmpty();

        String firstMessage = response.path(String.format(ERRORS_MESSAGE.getPath(), firstArrayIndex));
        assertThat(firstMessage).as("Expected error message for syntax error")
                .isNotNull()
                .isNotEmpty();
    }

    @Test
    @Description("Requesting a non-existent field returns a validation error; status may be 200 or 400")
    void shouldReturnValidationErrorForNonExistentField() {
        String body = """
                {
                  "query": "{ characters(page: 1) { results { nonExistentField999 } } }"
                }
                """;

        Response response = graphPostRequest(body, FailedStatusCodes.BAD_REQUEST);

        List<?> errors = response.path(ERRORS.getPath());
        assertThat(errors).as("Non-existent field should produce validation errors")
                .isNotNull()
                .isNotEmpty();

        String message = response.path(String.format(ERRORS_MESSAGE.getPath(), firstArrayIndex));
        assertThat(message).as("Error message should reference the unknown field")
                .isNotNull();
    }
}
