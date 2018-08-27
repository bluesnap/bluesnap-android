package com.bluesnap.android.demoapp;

import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.bluesnap.androidapi.models.SdkRequest;
import com.bluesnap.androidapi.services.BSPaymentRequestException;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Created by sivani on 19/07/2018.
 */
@RunWith(AndroidJUnit4.class)

public class FullBillingWithShippingTests extends EspressoBasedTest {

    @Before
    public void setup() throws InterruptedException, BSPaymentRequestException {
        SdkRequest sdkRequest = new SdkRequest(roundedPurchaseAmount, checkoutCurrency);
        sdkRequest.getShopperCheckoutRequirements().setBillingRequired(true);
        sdkRequest.getShopperCheckoutRequirements().setShippingRequired(true);
        setupAndLaunch(sdkRequest);
        onView(withId(R.id.newCardButton)).perform(click());
    }

    @Test
    public void full_billing_with_shipping_test() {
        //Pre-condition: credit card number wasn't entered
        new_cc_info_visibility_validation();
        new_credit_card_info_error_messages_validation();
        new_credit_billing_contact_info_visibility_validation();
        new_credit_billing_contact_info_error_messages_validation();
        //Pre-condition: Current billing country is the default one
        default_country_zip_view_validation_in_billing();
        default_country_state_view_validation_in_billing();
        shipping_button_validation();

        TestUtils.continueToShippingOrPayInNewCard(defaultCountryKey, true, false);
        //Pre-condition: The current fragment displayed is shipping
        new_credit_shipping_contact_info_visibility_validation();
        new_credit_shipping_contact_info_error_messages_validation();
        //Pre-condition: Current country is the default one
        default_country_zip_view_validation_in_shipping();
        default_country_state_view_validation_in_shipping();
        pay_button_in_shipping_validation();

        TestUtils.goBackToBillingInNewCard();

        //Pre-condition: Current country is the default one
        country_changes_per_fragment_validation();
        amount_tax_view_before_choosing_shipping_same_as_billing();
        onView(withId(R.id.shippingSameAsBillingSwitch)).perform(swipeLeft()); //annul shipping same as billing option
        amount_tax_view_after_choosing_shipping_same_as_billing();
    }

    @Test
    public void full_billing_with_shipping_basic_flow_transaction() throws InterruptedException {
        new_card_basic_flow_transaction(true, false, true, false);
    }

    @Test
    public void returning_shopper_full_billing_with_shipping_basic_flow_transaction() throws BSPaymentRequestException, InterruptedException {
        //make transaction to create a new shopper
        new_card_basic_flow_transaction(true, false, true, false);

        //setup sdk for the returning shopper
        returningShopperSetUp(true, false, true);

        //make a transaction with the returning shopper
        returning_shopper_card_basic_flow_transaction(true, false, true);
    }

    /**
     * This test verifies that all the credit card fields are displayed as they should
     * when choosing new credit card.
     */
    public void new_cc_info_visibility_validation() {
        CreditCardVisibilityTesterCommon.new_credit_card_info_visibility_validation("new_cc_info_visibility_validation");
    }

    /**
     * This test verifies that all invalid error messages of credit card info
     * fields are not displayed.
     */
    public void new_credit_card_info_error_messages_validation() {
        CreditCardVisibilityTesterCommon.new_credit_card_info_error_messages_validation("new_credit_card_info_error_messages_validation");
    }

    /**
     * This test verifies that all the billing contact info fields are displayed
     * according to full billing with shipping when choosing new credit card.
     */
    public void new_credit_billing_contact_info_visibility_validation() {
        CreditCardVisibilityTesterCommon.contact_info_visibility_validation("new_credit_billing_contact_info_visibility_validation", R.id.billingViewComponent, true, false);
    }

    /**
     * This test verifies that all invalid error messages of billing contact info
     * fields are not displayed.
     */
    public void new_credit_billing_contact_info_error_messages_validation() {
        CreditCardVisibilityTesterCommon.contact_info_error_messages_validation("contact_info_error_messages_validation", R.id.billingViewComponent, defaultCountryKey, true, false);
    }

    /**
     * This test verifies that all the shipping contact info fields are displayed
     * according to shipping enabled when choosing new credit card.
     */
    public void new_credit_shipping_contact_info_visibility_validation() {
        CreditCardVisibilityTesterCommon.contact_info_visibility_validation("new_credit_shipping_contact_info_visibility_validation", R.id.newShoppershippingViewComponent, true, false);
    }

    /**
     * This test verifies that all invalid error messages of billing contact info
     * fields are not displayed.
     */
    public void new_credit_shipping_contact_info_error_messages_validation() {
        CreditCardVisibilityTesterCommon.contact_info_error_messages_validation("contact_info_error_messages_validation", R.id.billingViewComponent, defaultCountryKey, true, false);
    }

    /**
     * This test checks whether the zip field is visible to the user or not, according
     * to the default Country (the one that is chosen when entering billing).
     */
    public void default_country_zip_view_validation_in_billing() {
        CreditCardVisibilityTesterCommon.default_country_zip_view_validation("default_country_zip_view_validation_in_billing", defaultCountryKey, R.id.billingViewComponent);
    }

    /**
     * This test checks whether the state field is visible to the user or not, according
     * to the default Country (the one that is chosen when entering billing).
     * If the country is USA, Canada or Brazil, then it should be visible,
     * o.w. it doesn't.
     */
    public void default_country_state_view_validation_in_billing() {
        CreditCardVisibilityTesterCommon.default_country_state_view_validation("default_country_state_view_validation_in_billing", R.id.billingViewComponent, defaultCountryKey);
    }

    /**
     * This test checks whether the zip field is visible to the user or not, according
     * to the default Country (the one that is chosen when entering shipping).
     */
    public void default_country_zip_view_validation_in_shipping() {
        CreditCardVisibilityTesterCommon.default_country_zip_view_validation("default_country_zip_view_validation_in_shipping", defaultCountryKey, R.id.newShoppershippingViewComponent);
    }

    /**
     * This test checks whether the state field is visible to the user or not, according
     * to the default Country (the one that is chosen when entering shipping).
     * If the country is USA, Canada or Brazil, then it should be visible,
     * o.w. it doesn't.
     */
    public void default_country_state_view_validation_in_shipping() {
        CreditCardVisibilityTesterCommon.default_country_state_view_validation("default_country_state_view_validation_in_shipping", R.id.newShoppershippingViewComponent, defaultCountryKey);
    }

    /**
     * This test verifies that the "Pay" button is visible and contains
     * the correct currency symbol and amount
     */

    public void pay_button_in_shipping_validation() {
        double tax = defaultCountryKey.equals("US") ? taxAmount : 0.00;
        CreditCardVisibilityTesterCommon.pay_button_visibility_and_content_validation("pay_button_in_shipping_validation", R.id.shippingButtonComponentView, checkoutCurrency, roundedPurchaseAmount, tax);
    }

    /**
     * This test verifies that the "Shipping" button is visible
     */
    public void shipping_button_validation() {
        CreditCardVisibilityTesterCommon.shipping_button_visibility_and_content_validation("shipping_button_validation", R.id.billingButtonComponentView);
    }

    /**
     * This test verifies that changing the country in billing
     * doesn't change the country in shipping as well, and vice versa.
     */
    public void country_changes_per_fragment_validation() {
        CreditCardVisibilityTesterCommon.country_changes_per_fragment_validation("country_changes_per_fragment_validation");
    }

    /**
     * This test verifies that the amount tax shipping component is visible when
     * using shipping same as billing, after choosing USA (which has shipping tax),
     * and that it presents the right amount and tax.
     * It also verifies that the component isn't presented any longer after changing
     * to a country without tax.
     */
    public void amount_tax_view_before_choosing_shipping_same_as_billing() {
        //choose United States for shipping tax
        ContactInfoTesterCommon.changeCountry(R.id.billingViewComponent, "United States");

        onView(withId(R.id.shippingSameAsBillingSwitch)).perform(swipeRight()); //choose shipping same as billing option

        //verify that the amount tax shipping component is presented
        CreditCardVisibilityTesterCommon.amount_tax_shipping_view_validation("amount_tax_view_before_choosing_shipping_same_as_billing", R.id.amountTaxShippingComponentView, checkoutCurrency,
                TestUtils.getAmountInString(df, roundedPurchaseAmount), TestUtils.getAmountInString(df, taxAmount));

        //change to Spain- a country without shipping tax
        ContactInfoTesterCommon.changeCountry(R.id.billingViewComponent, "Spain");

        //verify that the amount tax shipping component isn't displayed
        onView(allOf(withId(R.id.amountTaxLinearLayout), isDescendantOfA(withId(R.id.amountTaxShippingComponentView))))
                .withFailureHandler(new CustomFailureHandler("amount_tax_view_before_choosing_shipping_same_as_billing: Amount-tax component is displayed in a country without tax"))
                .check(matches(not(ViewMatchers.isDisplayed())));
    }

    /**
     * This test verifies that the amount tax shipping component isn't presented when changing
     * to a country without tax and choosing shipping same as billing.
     * Than it verifies that after choosing USA (which has shipping tax),
     * the amount tax shipping component is visible and it presents the right amount and tax.
     */
    public void amount_tax_view_after_choosing_shipping_same_as_billing() {
        //change to Costa Rica- a country without shipping tax
        ContactInfoTesterCommon.changeCountry(R.id.billingViewComponent, "Costa Rica");

        onView(withId(R.id.shippingSameAsBillingSwitch)).perform(swipeRight()); //choose shipping same as billing option

        //verify that the amount tax shipping component isn't displayed
        onView(allOf(withId(R.id.amountTaxLinearLayout), isDescendantOfA(withId(R.id.amountTaxShippingComponentView))))
                .withFailureHandler(new CustomFailureHandler("amount_tax_view_after_choosing_shipping_same_as_billing: Amount-tax component is displayed in a country without tax"))
                .check(matches(not(ViewMatchers.isDisplayed())));

        //change to United States, which has shipping tax
        ContactInfoTesterCommon.changeCountry(R.id.billingViewComponent, "United States");

        //verify that the amount tax shipping component is presented
        CreditCardVisibilityTesterCommon.amount_tax_shipping_view_validation("amount_tax_view_after_choosing_shipping_same_as_billing", R.id.amountTaxShippingComponentView, checkoutCurrency,
                TestUtils.getAmountInString(df, roundedPurchaseAmount), TestUtils.getAmountInString(df, taxAmount));

    }

}
