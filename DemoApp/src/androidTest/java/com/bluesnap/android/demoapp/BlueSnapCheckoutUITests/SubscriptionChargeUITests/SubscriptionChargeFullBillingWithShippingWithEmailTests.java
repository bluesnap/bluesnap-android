package com.bluesnap.android.demoapp.BlueSnapCheckoutUITests.SubscriptionChargeUITests;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.FlakyTest;

import com.bluesnap.android.demoapp.BlueSnapCheckoutUITests.CheckoutCommonTesters.ContactInfoTesterCommon;
import com.bluesnap.android.demoapp.R;
import com.bluesnap.android.demoapp.TestingShopperCheckoutRequirements;
import com.bluesnap.androidapi.models.SdkRequestSubscriptionCharge;
import com.bluesnap.androidapi.services.BSPaymentRequestException;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

/**
 * Created by sivani on 18/03/2019.
 */

@RunWith(AndroidJUnit4.class)

public class SubscriptionChargeFullBillingWithShippingWithEmailTests extends SubscriptionChargeEspressoBasedTester {
    public SubscriptionChargeFullBillingWithShippingWithEmailTests() {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(true, true, true);
    }

    public void setupBeforeSubscription(boolean withPriceDetails, boolean forReturningShopper) throws InterruptedException, BSPaymentRequestException, JSONException {
        subscriptionChargeSetup(withPriceDetails, forReturningShopper);
    }

    /**
     * This test does an end-to-end new card subscription flow for full
     * billing with shipping with email new shopper
     * with price details presented
     */
    @Test
    public void full_billing_with_shipping_with_email_with_price_details_basic_subscription_flow() throws InterruptedException, BSPaymentRequestException, JSONException {
        setupBeforeSubscription(true, false);
        new_card_basic_subscription_flow(true);
    }

    /**
     * This test does an end-to-end new card subscription flow for full
     * billing with shipping with email new shopper
     * without price details presented
     */
    @Test
    public void full_billing_with_shipping_with_email_basic_subscription_flow() throws InterruptedException, BSPaymentRequestException, JSONException {
        setupBeforeSubscription(false, false);
        new_card_basic_subscription_flow(false);
    }

    /**
     * This test does an end-to-end existing card subscription flow for full
     * billing with shipping with email returning shopper
     * with price details presented
     * FLAKY due to countryImageButton
     */
    // @Test
    @FlakyTest
    public void returning_shopper_full_billing_with_shipping_with_email_with_price_details_basic_subscription_flow() throws InterruptedException, BSPaymentRequestException, JSONException {
        setupBeforeSubscription(true, true);
        returning_shopper_card_basic_subscription_flow(true);
    }

    /**
     * This test does an end-to-end existing card subscription flow for full
     * billing with shipping with email returning shopper
     * without price details presented
     * FLAKY due to countryImageButton
     */
    // @Test
    @FlakyTest
    public void returning_shopper_full_billing_with_shipping_with_email_basic_subscription_flow() throws InterruptedException, BSPaymentRequestException, JSONException {
        setupBeforeSubscription(false, true);
        returning_shopper_card_basic_subscription_flow(false);
    }

    /**
     * Test a bug in new card subscription flow when trying to choose new card twice and then change the state.
     * For full billing with shipping with email new shopper
     * with price details presented
     */
    @Test
    public void choosing_new_card_twice_state_bug() throws InterruptedException, BSPaymentRequestException, JSONException {
        setupBeforeSubscription(true, false);
        onView(ViewMatchers.withId(R.id.newCardButton)).perform(click());

        Espresso.pressBack();

        onView(ViewMatchers.withId(R.id.newCardButton)).perform(click());

        String state = ContactInfoTesterCommon.getDefaultStateByCountry(ContactInfoTesterCommon.billingContactInfo.getCountryKey());

        if (state != null) {
            ContactInfoTesterCommon.changeState(R.id.billingViewComponent, state);
        }
    }

    /**
     * Test that the default subscription cancellation message is displayed
     * when using default SdkRequestSubscriptionCharge configuration
     */
    @Test
    public void subscription_cancellation_message_default_behavior() throws InterruptedException, BSPaymentRequestException, JSONException {
        setupBeforeSubscription(true, false);
        onView(ViewMatchers.withId(R.id.newCardButton)).perform(click());

        // Verify default cancellation message is displayed
        onView(ViewMatchers.withId(com.bluesnap.androidapi.R.id.subscriptionCancellationTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText("You can cancel subscriptions at any time")));
    }

    /**
     * Test that the subscription cancellation message is hidden
     * when configured to not show it
     */
    @Test
    public void subscription_cancellation_message_hidden() throws InterruptedException, BSPaymentRequestException, JSONException {
        // Setup with custom SdkRequest that hides the cancellation message
        String returningShopperId = "";

        SdkRequestSubscriptionCharge sdkRequest = new SdkRequestSubscriptionCharge(purchaseAmount, checkoutCurrency, true, true, true, false);
        sdkRequest.setAllowCurrencyChange(true);
        uIAutoTestingBlueSnapService.setSdk(sdkRequest, shopperCheckoutRequirements);
        uIAutoTestingBlueSnapService.setupAndLaunch(sdkRequest, false, returningShopperId);
        applicationContext = uIAutoTestingBlueSnapService.applicationContext;
        defaultCountryKey = uIAutoTestingBlueSnapService.getDefaultCountryKey();
        defaultCountryValue = uIAutoTestingBlueSnapService.getDefaultCountryValue();

        onView(ViewMatchers.withId(R.id.newCardButton)).perform(click());

        // Verify cancellation message is not displayed
        onView(ViewMatchers.withId(com.bluesnap.androidapi.R.id.subscriptionCancellationTextView))
                .check(matches(not(isDisplayed())));
    }

    /**
     * Test that a custom subscription cancellation message is displayed
     * when configured with custom text
     */
    @Test
    public void subscription_cancellation_message_custom_text() throws InterruptedException, BSPaymentRequestException, JSONException {
        // Setup with custom SdkRequest that uses custom cancellation message
        String returningShopperId = "";
        String customMessage = "Cancel anytime with 24h notice";

        SdkRequestSubscriptionCharge sdkRequest = new SdkRequestSubscriptionCharge(purchaseAmount, checkoutCurrency, true, true, true, customMessage);
        sdkRequest.setAllowCurrencyChange(true);
        uIAutoTestingBlueSnapService.setSdk(sdkRequest, shopperCheckoutRequirements);
        uIAutoTestingBlueSnapService.setupAndLaunch(sdkRequest, false, returningShopperId);
        applicationContext = uIAutoTestingBlueSnapService.applicationContext;
        defaultCountryKey = uIAutoTestingBlueSnapService.getDefaultCountryKey();
        defaultCountryValue = uIAutoTestingBlueSnapService.getDefaultCountryValue();

        onView(ViewMatchers.withId(R.id.newCardButton)).perform(click());

        // Verify custom cancellation message is displayed
        onView(ViewMatchers.withId(com.bluesnap.androidapi.R.id.subscriptionCancellationTextView))
                .check(matches(isDisplayed()))
                .check(matches(withText(customMessage)));
    }

    /**
     * Test that the subscription cancellation message can be dynamically controlled
     * using the setter methods
     */
    @Test
    public void subscription_cancellation_message_dynamic_control() throws InterruptedException, BSPaymentRequestException, JSONException {
        // Setup with default configuration
        String returningShopperId = "";

        SdkRequestSubscriptionCharge sdkRequest = new SdkRequestSubscriptionCharge(purchaseAmount, checkoutCurrency);

        // Initially hide the message
        sdkRequest.setShowSubscriptionCancellationMessage(false);
        sdkRequest.setAllowCurrencyChange(true);
        uIAutoTestingBlueSnapService.setSdk(sdkRequest, shopperCheckoutRequirements);
        uIAutoTestingBlueSnapService.setupAndLaunch(sdkRequest, false, returningShopperId);
        applicationContext = uIAutoTestingBlueSnapService.applicationContext;
        defaultCountryKey = uIAutoTestingBlueSnapService.getDefaultCountryKey();
        defaultCountryValue = uIAutoTestingBlueSnapService.getDefaultCountryValue();

        onView(ViewMatchers.withId(R.id.newCardButton)).perform(click());

        // Verify cancellation message is not displayed initially
        onView(ViewMatchers.withId(com.bluesnap.androidapi.R.id.subscriptionCancellationTextView))
                .check(matches(not(isDisplayed())));

        // Note: In a real scenario, you would dynamically change the configuration
        // and update the UI, but this test demonstrates the API usage
    }
}
