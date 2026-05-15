package api.utils.enums;

import api.utils.interfaces.IStatusCode;
import lombok.Getter;

@Getter
public enum FailedStatusCodes implements IStatusCode {

    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    METHOD_NOT_ALLOWED(405),
    NOT_ACCEPTABLE(406),
    PROXY_AUTHENTICATION_REQUIRED(407),
    REQUEST_TIMEOUT(408),
    CONFLICT(409),
    GONE(410),
    LENGTH_REQUIRED(411),
    PRECONDITION_FAILED(412),
    REQUEST_ENTITY_TOO_LARGE(413),
    REQUEST_URL_TOO_LONG(414),
    UNSUPPORTED_MEDIA_TYPE(415),
    I_AM_A_TEAPOT(418),
    UNPROCESSABLE_ENTITY(422),
    TOO_MANY_REQUESTS(429),
    INTERNAL_SERVER_ERROR(500);

    private final int statusCode;

    FailedStatusCodes(int statusCode) {
        this.statusCode = statusCode;
    }
}

