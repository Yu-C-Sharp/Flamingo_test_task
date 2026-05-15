package api.utils.enums;

import api.utils.interfaces.IStatusCode;
import lombok.Getter;

@Getter
public enum SuccessStatusCodes implements IStatusCode {

    OK(200),
    CREATED(201),
    ACCEPTED(202),
    NON_AUTHORITATIVE_INFORMATION(203),
    NO_CONTENT(204),
    RESET_CONTENT(205);

    private final int statusCode;

    SuccessStatusCodes(int statusCode) {
        this.statusCode = statusCode;
    }
}
