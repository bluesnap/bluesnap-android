package com.bluesnap.android.demoapp.BlueSnapCheckoutUITests.CheckoutReturningShopperTests;

import com.bluesnap.android.demoapp.BlueSnapCheckoutUITests.CheckoutEspressoBasedTester;
import com.bluesnap.android.demoapp.TestingShopperCheckoutRequirements;
import com.bluesnap.androidapi.services.BSPaymentRequestException;

import org.json.JSONException;
import org.junit.Test;

/**
 * Created by sivani on 27/09/2018.
 */

public class ReturningShopperTransactionTests extends CheckoutEspressoBasedTester {

    /**
     * This test does an end-to-end new card with minimal billing
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_minimal_billing_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(false, false, false);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }

    /**
     * This test does an end-to-end new card with minimal billing and email
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_minimal_billing_with_email_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(false, true, false);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }

    /**
     * This test does an end-to-end new card with minimal billing and shipping
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_minimal_billing_with_shipping_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(false, false, true);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }

    /**
     * This test does an end-to-end new card with minimal billing, email and shipping
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_minimal_billing_with_email_with_shipping_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(false, true, true);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }

    /**
     * This test does an end-to-end new card with full billing
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_full_billing_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(true, false, false);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }

    /**
     * This test does an end-to-end new card with full billing and email
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_full_billing_with_email_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(true, true, false);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }

    /**
     * This test does an end-to-end new card with full billing and shipping
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_full_billing_with_shipping_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(true, false, true);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }

    /**
     * This test does an end-to-end new card with full billing, email and shipping
     * flow for returning shopper without credit card info.
     */
    @Test
    public void returning_shopper_new_credit_card_full_billing_with_email_with_shipping_transaction() throws InterruptedException, BSPaymentRequestException, JSONException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(true, true, true);

        returning_shopper_with_new_credit_card_basic_flow_transaction();
    }
}
