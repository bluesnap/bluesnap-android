package com.bluesnap.android.demoapp;

import java.time.Year;

/**
 * Created by sivani on 30/08/2018.
 */

public class TestingShopperCreditCard {

    static Year expyear = java.time.Year.now().plusYears(2);
    static String lastYearTwoDigits =  expyear.toString().substring(expyear.toString().length() -2);

    /**
     * see docs:
     * Frictionless test cases:  https://developer.cardinaltrusted.com/reference/frictionless-authentication-test-cases
     * Challenge test cases: https://developer.cardinaltrusted.com/reference/emv-3ds-test-cases-challenge
     *
     */

    public static final TestingShopperCreditCard VISA_CREDIT_CARD = new TestingShopperCreditCard("4111111111111111", "123", "1111",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);
    public static final TestingShopperCreditCard MASTERCARD_CREDIT_CARD = new TestingShopperCreditCard("5572758886015288", "123", "5288",
            "MASTERCARD", "DEBIT", 12, expyear.getValue(), lastYearTwoDigits);

    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_SUCCESS = new TestingShopperCreditCard("4000000000002503", "123", "1091",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_UNAVAILABLE = new TestingShopperCreditCard("4000000000002990", "123", "1059",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_NOT_SUPPORTED = new TestingShopperCreditCard("4000000000000002", "123", "0002",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_BYPASS = new TestingShopperCreditCard("4000000000002560", "123", "1133",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_FAILURE = new TestingShopperCreditCard("4000000000001109", "123", "1109",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    // Frictionless success - PAResStatus Y, no challenge required
    // Cardinal docs: https://developer.cardinaltrusted.com/reference/frictionless-authentication-test-cases
    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_FRICTIONLESS_SUCCESS = new TestingShopperCreditCard("4000000000002701", "123", "2701",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    // Rejected by issuer - PAResStatus R, frictionless flow
    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_REJECTED = new TestingShopperCreditCard("4000000000002537", "123", "2537",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    // Attempts/Stand-in - PAResStatus A, frictionless flow
    public static final TestingShopperCreditCard VISA_CREDIT_CARD_FOR_3DS_ATTEMPTS = new TestingShopperCreditCard("4000000000002719", "123", "2719",
            "VISA", "", 01, expyear.getValue(), lastYearTwoDigits);

    private String cardNumber;
    private String cvv;
    private String cardLastFourDigits;
    private String cardType;
    private String cardSubType;
    private int expirationMonth;
    private int expirationYear;
    private String expirationYearLastTwoDigit;

    public TestingShopperCreditCard(String cardNumber, String cvv, String cardLastFourDigits, String cardType,
                                    String cardSubType, int expirationMonth, int expirationYear, String expirationYearLastTwoDigit) {
        this.cardNumber = cardNumber;
        this.cvv = cvv;
        this.cardLastFourDigits = cardLastFourDigits;
        this.cardType = cardType;
        this.cardSubType = cardSubType;
        this.expirationMonth = expirationMonth;
        this.expirationYear = expirationYear;
        this.expirationYearLastTwoDigit = expirationYearLastTwoDigit;
    }

    public String getCvv() {
        return cvv;
    }

    public void setCvv(String cvv) {
        this.cvv = cvv;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCardLastFourDigits() {
        return cardLastFourDigits;
    }

    public void setCardLastFourDigits(String cardLastFourDigits) {
        this.cardLastFourDigits = cardLastFourDigits;
    }

    public String getCardType() {
        return cardType;
    }

    public void setCardType(String cardType) {
        this.cardType = cardType;
    }

    public String getCardSubType() {
        return cardSubType;
    }

    public void setCardSubType(String cardSubType) {
        this.cardSubType = cardSubType;
    }

    public int getExpirationMonth() {
        return expirationMonth;
    }

    public void setExpirationMonth(int expirationMonth) {
        this.expirationMonth = expirationMonth;
    }

    public int getExpirationYear() {
        return expirationYear;
    }

    public void setExpirationYear(int expirationYear) {
        this.expirationYear = expirationYear;
    }

    public String getExpirationYearLastTwoDigit() {
        return expirationYearLastTwoDigit;
    }

    public void setExpirationYearLastTwoDigit(String expirationYearLastTwoDigit) {
        this.expirationYearLastTwoDigit = expirationYearLastTwoDigit;
    }
}
