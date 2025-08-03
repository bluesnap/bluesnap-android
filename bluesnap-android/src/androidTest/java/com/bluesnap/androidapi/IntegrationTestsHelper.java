package com.bluesnap.androidapi;

import android.util.Log;

import com.bluesnap.androidapi.http.BlueSnapHTTPResponse;
import com.bluesnap.androidapi.models.BillingContactInfo;
import com.bluesnap.androidapi.models.CreditCard;
import com.bluesnap.androidapi.models.EcpAchDetails;
import com.bluesnap.androidapi.models.PurchaseDetails;
import com.bluesnap.androidapi.models.SdkResult;
import com.bluesnap.androidapi.services.BlueSnapService;
import com.bluesnap.androidapi.services.BlueSnapValidator;
import com.bluesnap.androidapi.services.BluesnapServiceCallback;
import com.bluesnap.androidapi.services.KountService;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

import static java.net.HttpURLConnection.HTTP_OK;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class IntegrationTestsHelper {
    private static String TAG;
    BlueSnapService blueSnapService = BlueSnapService.Companion.getInstance();

    public IntegrationTestsHelper(String tag) {
        TAG = tag;
    }

    public void endToEndCreditCardCheckoutFlow(Double amount, String currencyNameCode, String creditCard) throws InterruptedException {

        // Initialize billing info
        final BillingContactInfo billingContactInfo = new BillingContactInfo();
        billingContactInfo.setFullName("John Doe");

        // Initialize card info
        final CreditCard card = new CreditCard();
        String number = creditCard;
        card.update(number, "11/25", "123");

        // Initialize PurchaseDetails
        final PurchaseDetails purchaseDetails = new PurchaseDetails();
        purchaseDetails.setBillingContactInfo(billingContactInfo);
        purchaseDetails.setCreditCard(card);
        purchaseDetails.setStoreCard(true);

        //assertTrue("this should be a valid luhn", BlueSnapValidator.Companion.getInstance().creditCardNumberValidation(CARD_NUMBER_VALID_LUHN_UNKNOWN_TYPE));
//        assertTrue(BlueSnapValidator.Companion.getInstance().creditCardNumberValidation(CARD_NUMBER_VALID_LUHN_UNKNOWN_TYPE));
        assertTrue(BlueSnapValidator.Companion.getInstance().creditCardFullValidation(card));
        assertNotNull(card.getCardType());
        assertFalse(card.getCardType().isEmpty());

        try {
            BlueSnapHTTPResponse blueSnapHTTPResponse = blueSnapService.submitTokenizedDetails(purchaseDetails);
            assertEquals(HTTP_OK, blueSnapHTTPResponse.getResponseCode());

            JSONObject jsonObject = new JSONObject(blueSnapHTTPResponse.getResponseString());
            String Last4 = jsonObject.getString("last4Digits");
            String ccType = jsonObject.getString("ccType");
            assertEquals("MASTERCARD", ccType);
            assertEquals("1116", Last4);

        } catch (NullPointerException | JSONException e) {
            e.printStackTrace();
            fail("Exceptions while parsing response");
        }

        SdkResult sdkResult = blueSnapService.getSdkResult();
        sdkResult.setAmount(amount);
        sdkResult.setCurrencyNameCode(currencyNameCode);
        sdkResult.setToken(blueSnapService.getBlueSnapToken().getMerchantToken());
        sdkResult.setKountSessionId(KountService.Companion.getInstance().getKountSessionId());

        final Semaphore semaphore2 = new Semaphore(1);
        semaphore2.acquire();

        // making a transaction
        IntegrationTestsDemoTransactions transactions = IntegrationTestsDemoTransactions.getInstance();
        transactions.createCreditCardTransaction(sdkResult, new BluesnapServiceCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "Transaction success");
                semaphore2.release();
            }

            @Override
            public void onFailure() {
                semaphore2.release();
                fail("Failed to make a transaction");
            }
        });

        do {
            Thread.sleep(1000);
            Log.i(TAG, "Waiting for card transaction to finish");
        } while (!semaphore2.tryAcquire());

        Log.i(TAG, "Done");


    }


    public void endToEndAchPaymentFlow(Double amount, String currencyNameCode,String paymentMethod, String routingNumber, String accountNumber, String accountType) throws InterruptedException {
        // Initialize billing info
        final BillingContactInfo billingContactInfo = new BillingContactInfo();
        billingContactInfo.setFullName("John Doe");

        final EcpAchDetails ecpAchDetails = new EcpAchDetails(
                paymentMethod,
                routingNumber,
                accountNumber,
                accountType
        );

        // Validate ACH payment info
         assertTrue("ACH Payment Info validation failed", ecpAchDetails.getValid());

        try {
            BlueSnapHTTPResponse blueSnapHTTPResponse = blueSnapService.submitTokenizedEcpAchDetails(ecpAchDetails);
            assertEquals(HTTP_OK, blueSnapHTTPResponse.getResponseCode());

        } catch (Exception e) {
            Log.e(TAG, "Exception while processing ACH payment details", e);
            fail("Exception occurred during ACH payment processing");
        }

        SdkResult sdkResult = blueSnapService.getSdkResult();
        sdkResult.setAmount(amount);
        sdkResult.setCurrencyNameCode(currencyNameCode);
        sdkResult.setAccountNumber(accountNumber);
        sdkResult.setRoutingNumber(routingNumber);
        sdkResult.setAccountType(accountType);
        sdkResult.setPaymentMethod(paymentMethod);

        sdkResult.setToken(blueSnapService.getBlueSnapToken().getMerchantToken());
        sdkResult.setKountSessionId(KountService.Companion.getInstance().getKountSessionId());

        final CountDownLatch latch = new CountDownLatch(1);

        // Making an ACH transaction
        IntegrationTestsDemoTransactions transactions = IntegrationTestsDemoTransactions.getInstance();
        transactions.createEcpAchTransaction(sdkResult, new BluesnapServiceCallback() {
            @Override
            public void onSuccess() {
                Log.i(TAG, "ACH Transaction success");
                latch.countDown();
            }

            @Override
            public void onFailure() {
                Log.e(TAG, "Failed to make an ACH transaction");
                latch.countDown();
                fail("Failed to make an ACH transaction");
            }
        });

        // Waiting for the transaction to finish
        try {
            latch.await();
            Log.i(TAG, "ACH Transaction Done");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            Log.e(TAG, "Thread was interrupted", e);
        }
    }
}
