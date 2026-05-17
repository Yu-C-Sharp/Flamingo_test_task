package api.utils.enums;

import api.utils.interfaces.IPath;
import lombok.Getter;

@Getter
public enum ApiPaths implements IPath {

    AUTH("/auth"),
    BOOKING("/booking"),
    BOOKING_BY_ID("/booking/{id}"),
    BOOK_STORE_BOOKS("/BookStore/v1/Books");

    private final String path;

    ApiPaths(String path) {
        this.path = path;
    }
}
