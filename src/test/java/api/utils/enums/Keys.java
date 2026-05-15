package api.utils.enums;

import lombok.Getter;

@Getter
public enum Keys {

    BOOKING_ID("bookingid");

    private final String key;

    Keys(String key) {
        this.key = key;
    }
}
