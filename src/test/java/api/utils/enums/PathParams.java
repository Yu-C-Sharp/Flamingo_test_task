package api.utils.enums;

import lombok.Getter;

@Getter
public enum PathParams {

    ID("id");

    private final String param;

    PathParams(String param) {
        this.param = param;
    }
}
