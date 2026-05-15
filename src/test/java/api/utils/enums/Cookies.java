package api.utils.enums;

import lombok.Getter;

@Getter
public enum Cookies {

    TOKEN("token");

    private final String cookie;

    Cookies(String cookie) {
        this.cookie = cookie;
    }
}
