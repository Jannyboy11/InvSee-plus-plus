package com.janboerman.invsee.metrics;

import java.time.Duration;
import java.time.Instant;

public enum Uptime {

    LESS_THAN_ONE_DAY("< 1 day"),
    ONE_DAY_2_ONE_WEEK("1 day - 1 week"),
    ONE_WEEK_2_ONE_MONTH("1 week - 1 month"),
    ONE_MONTH_2_THREE_MONTHS("1 month - 3 months"),
    THREE_MONTHS_2_HALF_A_YEAR("3 months - 6 months"),
    HALF_A_YEAR_2_ONE_YEAR("6 months - 1 year"),
    ONE_YEAR_2_TWO_YEARS("1 year - 2 years"),
    TWO_YEARS_OR_MORE("> 2 years");

    private final String toString;

    private Uptime(String toString) {
        this.toString = toString;
    }

    public String toString() {
        return toString;
    }

    static Uptime calculate(Instant from, Instant to) {
        long days = Duration.between(from, to).toDays();
        if (days < 1) {
            return LESS_THAN_ONE_DAY;
        } else if (days < 7) {
            return ONE_DAY_2_ONE_WEEK;
        } else if (days < 30) {
            return ONE_WEEK_2_ONE_MONTH;
        } else if (days < 90) {
            return ONE_MONTH_2_THREE_MONTHS;
        } else if (days < 183) {
            return THREE_MONTHS_2_HALF_A_YEAR;
        } else if (days < 365) {
            return HALF_A_YEAR_2_ONE_YEAR;
        } else if (days < 730) {
            return ONE_YEAR_2_TWO_YEARS;
        } else {
            return TWO_YEARS_OR_MORE;
        }
    }
}
