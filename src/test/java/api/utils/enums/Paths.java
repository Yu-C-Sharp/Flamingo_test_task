package api.utils.enums;

import api.utils.interfaces.IPath;
import lombok.Getter;

@Getter
public enum Paths implements IPath {

    AUTH("/auth"),
    BOOKING("/booking"),
    BOOKING_BY_ID("/booking/{id}");

    private final String path;

    Paths(String path) {
        this.path = path;
    }
}
