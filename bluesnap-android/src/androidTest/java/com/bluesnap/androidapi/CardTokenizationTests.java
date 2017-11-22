package com.bluesnap.androidapi;

import android.os.Handler;
import android.os.Looper;

import com.bluesnap.androidapi.models.BillingInfo;
import com.bluesnap.androidapi.models.SdkRequest;
import com.bluesnap.androidapi.models.CreditCard;
import com.bluesnap.androidapi.models.CreditCardInfo;
import com.bluesnap.androidapi.models.Shopper;
import com.bluesnap.androidapi.services.BSPaymentRequestException;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import java.io.UnsupportedEncodingException;

import cz.msebera.android.httpclient.Header;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by oz on 10/30/17.
 */

public class CardTokenizationTests extends BSAndroidTestsBase {

    @Test
    public void tokenize_card_test() throws InterruptedException, BSPaymentRequestException, UnsupportedEncodingException, JSONException {
        final String CARD_NUMBER_VALID_LUHN_UNKNOWN_TYPE = "1234123412341238";
        final String CARD_NUMBER_VALID_LUHN_MASTERCARD_FAKED = "5568111111111116";

        SdkRequest sdkRequest = new SdkRequest();
        Double amount = 30.5D;
        sdkRequest.setAmount(amount);
        sdkRequest.setCurrencyNameCode("USD");
        blueSnapService.setSdkRequest(sdkRequest);

        final Shopper shopper = new Shopper();
        final CreditCardInfo creditCardInfo = shopper.getNewCreditCardInfo();
        final CreditCard card = creditCardInfo.getCreditCard();
        final BillingInfo billingInfo = creditCardInfo.getBillingContactInfo();
        String number = CARD_NUMBER_VALID_LUHN_MASTERCARD_FAKED;
        card.update(number, "11/50", "123");
        billingInfo.setFullName("John Doe");
        assertTrue("this should be a valid luhn", CreditCard.isValidLuhnNumber(CARD_NUMBER_VALID_LUHN_UNKNOWN_TYPE));
        assertTrue(card.validateNumber());
        assertTrue(card.validateAll());
        assertFalse(card.getCardType().isEmpty());
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            public void run() {
                try {
                    blueSnapService.tokenizeCard(shopper, new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                            try {
                                assertEquals(200, statusCode);
                                String Last4 = response.getString("last4Digits");
                                String ccType = response.getString("ccType");
                                assertEquals("MASTERCARD", ccType);
                                assertEquals("1116", Last4);

                            } catch (NullPointerException | JSONException e) {
                                e.printStackTrace();
                                ;
                                fail("Exceptions while parsing response");
                            }
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            fail("Could not tokenize card:" + statusCode);
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    fail("json exception:" + e);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    fail("got UnsupportedEncodingException");
                }
            }
        }, 100);

    }

}