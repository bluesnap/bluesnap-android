package com.bluesnap.androidapi;

import java.time.Year;

public final class TestExpirationDateUtils {
    private static final int YEARS_IN_FUTURE = 2;

    private TestExpirationDateUtils() {}

    /** Returns future year as 2-digit int (e.g., 28 for 2028) */
    public static int getFutureYearTwoDigit() {
        return Year.now().plusYears(YEARS_IN_FUTURE).getValue() % 100;
    }

    /** Returns expiration date string in MM/YY format */
    public static String getExpirationDateString(int month) {
        return String.format("%02d/%02d", month, getFutureYearTwoDigit());
    }

    /** Returns default "11/YY" expiration date string */
    public static String getDefaultExpirationDateString() {
        return getExpirationDateString(11);
    }
}
