package utils.enums;

import lombok.Getter;

@Getter
public enum DatePatterns {

    YEAR_MONTH_DAY("yyyy-MM-dd");

    private final String pattern;

    DatePatterns(String pattern) {
        this.pattern = pattern;
    }
}
