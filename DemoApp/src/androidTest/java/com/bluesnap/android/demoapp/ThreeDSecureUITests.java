package com.bluesnap.android.demoapp;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import android.widget.EditText;

import com.bluesnap.android.demoapp.BlueSnapCheckoutUITests.CheckoutEspressoBasedTester;
import com.bluesnap.androidapi.services.BSPaymentRequestException;
import com.bluesnap.androidapi.services.CardinalManager;

import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static java.lang.Thread.sleep;
import static org.hamcrest.CoreMatchers.anything;
import static org.hamcrest.core.IsNull.notNullValue;

public class ThreeDSecureUITests extends CheckoutEspressoBasedTester {

    private static final String BASIC_SAMPLE_PACKAGE
            = "com.bluesnap.android.demoapp";
    private static final int LAUNCH_TIMEOUT = 5000;
    private static final String STRING_TO_BE_TYPED = "UiAutomator";
    private UiDevice mDevice;

    public ThreeDSecureUITests() {
        mDevice = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation());
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements();
    }

    public void setupBeforeTransaction(boolean fullBillingRequired, boolean emailRequired, boolean shippingRequired) throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements.setTestingShopperCheckoutRequirements(fullBillingRequired, emailRequired, shippingRequired, false);

        checkoutSetup(true, false, false, true);
        onView(ViewMatchers.withId(R.id.newCardButton)).perform(click());
    }

    public void setupForReturningShopperBeforeTransaction(boolean fullBillingRequired, boolean emailRequired, boolean shippingRequired, TestingShopperCreditCard creditCard) throws InterruptedException, BSPaymentRequestException, JSONException {
        //make transaction to create a new shopper
        uIAutoTestingBlueSnapService.createVaultedShopper(creditCard);

        shopperCheckoutRequirements.setTestingShopperCheckoutRequirements(fullBillingRequired, emailRequired, shippingRequired, false);

        //setup sdk for the returning shopper
        uIAutoTestingBlueSnapService.returningShopperSetUp(shopperCheckoutRequirements, true);

        onData(anything()).inAdapterView(withId(R.id.oneLineCCViewComponentsListView)).atPosition(0).perform(click());
    }

    /**
     * This test does an end-to-end checkout with 3DS flow
     * for success credit card
     * with minimal billing/
     * <p>
     * It runs in test mode.
     */
    @Test
    public void threeDS_success_minimal_billing_basic_transaction() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_SUCCESS, true, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_SUCCEEDED.name());
    }

    /**
     * This test does an end-to-end checkout with 3DS flow
     * for success credit card
     * with full billing, shipping and email.
     * <p>
     * It runs in test mode.
     */
    @Test
    public void threeDS_success_full_billing_with_email_with_shipping_basic_transaction() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(true, true, true);
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_SUCCESS, true, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_SUCCEEDED.name());
    }

    /**
     * This test does an end-to-end checkout with 3DS flow
     * for bypass credit card
     * with minimal billing.
     * <p>
     * It runs in test mode.
     */
    @Test
    public void threeDS_bypass_minimal_billing_basic_transaction() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_BYPASS, false, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_BYPASSED.name());
    }

    /**
     * This test does an end-to-end checkout with 3DS flow
     * for unavailable credit card
     * with minimal billing.
     * <p>
     * It runs in test mode.
     */
    @Test
    public void threeDS_unavailable_minimal_billing_basic_transaction() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_UNAVAILABLE, false, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_UNAVAILABLE.name());
    }

    /**
     * This test does an end-to-end checkout with 3DS flow
     * for unsupported credit card
     * with minimal billing.
     * <p>
     * It runs in test mode.
     */
    @Ignore("No Cardinal test card triggers CARD_NOT_SUPPORTED - requires 3DS 1.x card which test env doesn't provide")
    @Test
    public void threeDS_unsupported_minimal_billing_basic_transaction() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_NOT_SUPPORTED, false, CardinalManager.ThreeDSManagerResponse.CARD_NOT_SUPPORTED.name());
    }

    /**
     * This test does an end-to-end checkout with 3DS flow
     * for failure credit card
     * with minimal billing.
     * <p>
     * It runs in test mode.
     */
    @Test
    public void threeDS_failure_minimal_billing_basic_transaction() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_FAILURE, true, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_FAILED.name(), false);
    }

    /**
     * This test does an end-to-end checkout with 3DS flow
     * for success credit card
     * with minimal billing/
     * <p>
     * It runs in test mode.
     */
//    @Test
    public void threeDS_success_vaulted_card_minimal_billing_basic_transaction() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupForReturningShopperBeforeTransaction(false, false, false, TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_SUCCESS);
//        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_SUCCESS, true, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_SUCCEEDED.name());
    }

    /**
     * TDD Test: Frictionless success (PAResStatus: Y)
     * Card: 4000000000002701 (Visa 2.2.0)
     * Cardinal behavior: Success without challenge
     * Expected: AUTHENTICATION_SUCCEEDED
     */
    @Test
    public void threeDS_frictionless_success_minimal_billing() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_FRICTIONLESS_SUCCESS, false, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_SUCCEEDED.name());
    }

    /**
     * TDD Test: Rejected authentication (PAResStatus: R)
     * Card: 4000000000002537 (Visa 2.2.0)
     * Cardinal behavior: Issuer explicitly rejects authentication
     * Flow: Frictionless (no challenge)
     * Hypothesis: AUTHENTICATION_FAILED (TDD will verify actual BlueSnap response)
     */
    @Test
    public void threeDS_rejected_minimal_billing() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        // TDD hypothesis: Rejected maps to AUTHENTICATION_FAILED
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_REJECTED, false, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_FAILED.name(), false);
    }

    /**
     * TDD Test: Attempts/Stand-in (PAResStatus: A)
     * Card: 4000000000002719 (Visa 2.2.0)
     * Cardinal behavior: Issuer didn't respond, attempt recorded
     * Flow: Frictionless (no challenge)
     * Hypothesis: AUTHENTICATION_SUCCEEDED (TDD will verify actual BlueSnap response)
     */
    @Test
    public void threeDS_attempts_minimal_billing() throws UiObjectNotFoundException, InterruptedException, JSONException, BSPaymentRequestException {
        setupBeforeTransaction(false, false, false);
        // TDD hypothesis: Attempts may succeed (liability shift to issuer)
        basic3DSFlow(TestingShopperCreditCard.VISA_CREDIT_CARD_FOR_3DS_ATTEMPTS, false, CardinalManager.ThreeDSManagerResponse.AUTHENTICATION_SUCCEEDED.name());
    }

    private void basic3DSFlow(TestingShopperCreditCard creditCard, boolean isChallengeRequired, String expected3DSResult) throws UiObjectNotFoundException, InterruptedException {
        basic3DSFlow(creditCard, isChallengeRequired, expected3DSResult, true);
    }

    private void basic3DSFlow(TestingShopperCreditCard creditCard, boolean isChallengeRequired, String expected3DSResult, boolean isResultOK) throws UiObjectNotFoundException, InterruptedException {

        int buttonComponent = (shopperCheckoutRequirements.isShippingRequired() && !shopperCheckoutRequirements.isShippingSameAsBilling()) ? R.id.shippingButtonComponentView : R.id.billingButtonComponentView;
        //onView(withId(R.id.newCardButton)).perform(click());

        new_card_basic_fill_info(creditCard);

        TestUtils.pressBuyNowButton(buttonComponent);

        if (isChallengeRequired) {
            // Wait for either the OK dialog or the Cardinal challenge SUBMIT button to appear
            UiObject okButton = mDevice.findObject(new UiSelector().text("OK"));
            UiObject threeDSSubmitButton = mDevice.findObject(new UiSelector().text("SUBMIT"));

            // Wait up to 30 seconds for either OK button or SUBMIT button to appear
            int maxWaitSeconds = 30;
            for (int i = 0; i < maxWaitSeconds; i++) {
                if (okButton.exists() || threeDSSubmitButton.exists()) {
                    break;
                }
                sleep(1000);
            }

            // If OK button appeared, click it first
            if (okButton.exists()) {
                okButton.click();
                // Now wait for SUBMIT button
                while (!threeDSSubmitButton.exists())
                    sleep(2000);
            }

            // Cardinal SDK uses custom obfuscated EditText class (com.cardinalcommerce.a.setLeft)
            // with resource ID 'codeEditTextField' - must use resourceId selector instead of className
            UiObject otpField = mDevice.findObject(new UiSelector()
                    .resourceId("com.bluesnap.android.demoapp:id/codeEditTextField"));

            // Click OTP field first to focus it
            otpField.click();
            sleep(500);

            // Enter OTP code via shell input since setText doesn't work on Cardinal's custom widget
            // This is safe as we're using a hardcoded string "1234" in test code
            try {
                mDevice.executeShellCommand("input text 1234");
            } catch (java.io.IOException e) {
                // Fallback to setText if shell command fails
                otpField.setText("1234");
            }
            sleep(1000);

            // press submit button in cardinal activity
            threeDSSubmitButton.click();
        }

        uIAutoTestingBlueSnapService.finishDemoPurchase(shopperCheckoutRequirements, expected3DSResult, isResultOK);

    }

}
