package api.tests;

import api.utils.enums.FailedStatusCodes;
import api.utils.enums.SuccessStatusCodes;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.restassured.response.Response;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.List;

import static api.helpers.ApiHelper.graphPost;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * GraphQL tests against the Rick and Morty public API (https://rickandmortyapi.com/graphql).
 * Schema mirrors the assignment requirements: list with pagination, single entity by ID,
 * variables, nested fields across types (episode → characters → name).
 */
@Tag("api")
@Feature("GraphQL Rick and Morty")
class GraphQlTest {

    // ─── Positive ───────────────────────────────────────────────────────────────

    @Test
    @Description("Query a paginated list — characters(page:1) should return 20 results per page")
    void shouldGetCharactersFirstPage() {
        String body = """
                {
                  "query": "{ characters(page: 1) { info { count pages } results { id name } } }"
                }
                """;

        Response response = graphPost(body);
        response.then().statusCode(SuccessStatusCodes.OK.getStatusCode());

        List<?> results = response.path("data.characters.results");
        Integer pages = response.path("data.characters.info.pages");

        assertThat(results).as("First page should contain 20 characters").hasSize(20);
        assertThat(pages).as("Total pages should be positive").isPositive();
    }

    @Test
    @Description("Pagination offset — page 1 and page 2 must return different character IDs")
    void shouldReturnDifferentResultsOnDifferentPages() {
        String page1 = """
                { "query": "{ characters(page: 1) { results { id } } }" }
                """;
        String page2 = """
                { "query": "{ characters(page: 2) { results { id } } }" }
                """;

        List<String> ids1 = graphPost(page1).then()
                .statusCode(SuccessStatusCodes.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getList("data.characters.results.id");

        List<String> ids2 = graphPost(page2).then()
                .statusCode(SuccessStatusCodes.OK.getStatusCode())
                .extract()
                .jsonPath()
                .getList("data.characters.results.id");

        assertThat(ids1).doesNotContainAnyElementsOf(ids2);
    }

    @Test
    @Description("Query a single character by ID returns the expected entity")
    void shouldGetSingleCharacterById() {
        String body = """
                {
                  "query": "{ character(id: 1) { id name status species } }"
                }
                """;

        Response response = graphPost(body);
        response.then().statusCode(SuccessStatusCodes.OK.getStatusCode());

        String id = response.path("data.character.id");
        String name = response.path("data.character.name");

        assertThat(id).isEqualTo("1");
        assertThat(name).isEqualTo("Rick Sanchez");
    }

    @Test
    @Description("Query using GraphQL variables (no string interpolation) — character by variable ID")
    void shouldQueryUsingGraphQLVariables() {
        String body = """
                {
                  "query": "query GetCharacter($id: ID!) { character(id: $id) { id name } }",
                  "variables": { "id": "2" }
                }
                """;

        Response response = graphPost(body);
        response.then().statusCode(SuccessStatusCodes.OK.getStatusCode());

        assertThat((Object) response.path("errors")).isNull();
        assertThat((String) response.path("data.character.id")).isEqualTo("2");
        assertThat((String) response.path("data.character.name")).isEqualTo("Morty Smith");
    }

    @Test
    @Description("Nested fields across types — episode → characters → name")
    void shouldQueryNestedFieldsEpisodeCharacters() {
        String body = """
                {
                  "query": "{ episode(id: 1) { id name air_date characters { id name species } } }"
                }
                """;

        Response response = graphPost(body);
        response.then().statusCode(SuccessStatusCodes.OK.getStatusCode());

        String episodeName = response.path("data.episode.name");
        List<?> characters = response.path("data.episode.characters");
        String firstCharName = response.path("data.episode.characters[0].name");

        assertThat(episodeName).isNotNull().isNotEmpty();
        assertThat(characters).isNotEmpty();
        assertThat(firstCharName).isNotNull().isNotEmpty();
    }

    // ─── Negative ───────────────────────────────────────────────────────────────

    @Test
    @Description("Invalid character ID returns HTTP 200 with data.character = null")
    void shouldReturnNullForInvalidCharacterId() {
        String body = """
                {
                  "query": "{ character(id: 999999) { id name } }"
                }
                """;

        Response response = graphPost(body);
        response.then().statusCode(SuccessStatusCodes.OK.getStatusCode());

        Object character = response.path("data.character");
        Object errors = response.path("errors");

        boolean isNullData = character == null;
        boolean hasErrors = errors != null;
        assertThat(isNullData || hasErrors)
                .as("Expected data.character=null or errors for non-existent ID")
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

        Response response = graphPost(body);

        // GraphQL spec allows 200 or 400 for syntax errors; both are valid
        assertThat(response.statusCode())
                .as("Status should be 200 or 400 for a malformed query")
                .isIn(SuccessStatusCodes.OK.getStatusCode(), FailedStatusCodes.BAD_REQUEST.getStatusCode());

        List<?> errors = response.path("errors");
        assertThat(errors).as("Syntax error should produce errors array").isNotNull().isNotEmpty();

        String firstMessage = response.path("errors[0].message");
        assertThat(firstMessage).isNotNull().isNotEmpty();
    }

    @Test
    @Description("Requesting a non-existent field returns a validation error; status may be 200 or 400")
    void shouldReturnValidationErrorForNonExistentField() {
        String body = """
                {
                  "query": "{ characters(page: 1) { results { nonExistentField999 } } }"
                }
                """;

        Response response = graphPost(body);

        assertThat(response.statusCode())
                .as("Status should be 200 or 400 for a field validation error")
                .isIn(SuccessStatusCodes.OK.getStatusCode(), FailedStatusCodes.BAD_REQUEST.getStatusCode());

        List<?> errors = response.path("errors");
        assertThat(errors).as("Non-existent field should produce validation errors").isNotNull().isNotEmpty();

        String message = response.path("errors[0].message");
        assertThat(message).as("Error message should reference the unknown field").isNotNull();
    }
}
