package api.helpers;

import api.utils.enums.Cookies;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

public class ApiSender {

    static Response apiPost(RequestSpecification spec, String body, String endpoint) {
        return spec
                .body(body)
                .when()
                .post(endpoint)
                .then().log().all()
                .extract()
                .response();
    }

    static Response graphQlPost(RequestSpecification spec, String body) {
        return spec
                .body(body)
                .when()
                .post()
                .then().log().all()
                .extract()
                .response();
    }

    static Response get(RequestSpecification spec, String endpoint) {
        return spec
                .when()
                .get(endpoint)
                .then().log().all()
                .extract()
                .response();
    }

    static Response getWithPathParams(RequestSpecification spec, Map<String, ?> pathParams, String endpoint) {
        return spec
                .when()
                .pathParams(pathParams)
                .get(endpoint)
                .then().log().all()
                .extract()
                .response();
    }

    static Response getWithQueryParams(RequestSpecification spec, Map<String, ?> queryParams, String endpoint) {
        return spec
                .when()
                .queryParams(queryParams)
                .get(endpoint)
                .then().log().all()
                .extract()
                .response();
    }

    static Response put(RequestSpecification spec, String body, Map<String, ?> params, String endpoint, String authToken) {

        if (authToken != null) {
            spec.cookie(Cookies.TOKEN.getCookie(), authToken);
        }

        return spec
                .body(body)
                .when()
                .pathParams(params)
                .put(endpoint)
                .then().log().all()
                .extract()
                .response();
    }

    static Response delete(RequestSpecification spec, String authToken, Map<String, ?> params, String endpoint) {
        return spec
                .cookie(Cookies.TOKEN.getCookie(), authToken)
                .when()
                .pathParams(params)
                .delete(endpoint)
                .then().log().all()
                .extract()
                .response();
    }
}
