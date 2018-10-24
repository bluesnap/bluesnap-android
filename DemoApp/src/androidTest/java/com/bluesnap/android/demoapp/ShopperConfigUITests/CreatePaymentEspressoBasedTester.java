package com.bluesnap.android.demoapp.ShopperConfigUITests;

import android.support.test.rule.ActivityTestRule;

import com.bluesnap.android.demoapp.BlueSnapCheckoutUITests.WebViewUITests.PayPalWebViewTests;
import com.bluesnap.android.demoapp.TestingShopperCheckoutRequirements;
import com.bluesnap.android.demoapp.TestingShopperCreditCard;
import com.bluesnap.android.demoapp.UIAutoTestingBlueSnapService;
import com.bluesnap.androidapi.models.SdkRequest;
import com.bluesnap.androidapi.services.BSPaymentRequestException;
import com.bluesnap.androidapi.views.activities.BluesnapCreatePaymentActivity;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import static java.lang.Thread.sleep;

/**
 * Created by sivani on 06/09/2018.
 */

public class CreatePaymentEspressoBasedTester {
    String RETURNING_SHOPPER_FULL_BILLING_WITH_SHIPPING_CREDIT_CARD = "22973121";
    String RETURNING_SHOPPER_PAY_PAL = "23071553";

    protected String checkoutCurrency;
    protected double purchaseAmount;

    protected TestingShopperCheckoutRequirements shopperCheckoutRequirements;

    PayPalWebViewTests payPalWebViewTests = new PayPalWebViewTests();

    @Rule
    public ActivityTestRule<BluesnapCreatePaymentActivity> mActivityRule = new ActivityTestRule<>(
            BluesnapCreatePaymentActivity.class, false, false);

    protected UIAutoTestingBlueSnapService<BluesnapCreatePaymentActivity> uIAutoTestingBlueSnapService = new UIAutoTestingBlueSnapService<>(mActivityRule);

    public CreatePaymentEspressoBasedTester() {
        checkoutCurrency = uIAutoTestingBlueSnapService.getCheckoutCurrency();
        purchaseAmount = uIAutoTestingBlueSnapService.getPurchaseAmount();
    }

    protected void createPaymentSetup(String VaultedShopperID) throws BSPaymentRequestException, InterruptedException, JSONException {
        SdkRequest sdkRequest = new SdkRequest(purchaseAmount, checkoutCurrency);
        uIAutoTestingBlueSnapService.setSdk(sdkRequest, shopperCheckoutRequirements);

        uIAutoTestingBlueSnapService.setupAndLaunch(sdkRequest, true, VaultedShopperID);
    }

    /**
     * This test does a full billing with email and shipping
     * end-to-end create payment flow for the chosen card.
     * <p>
     * pre-condition: chosen card is TestingShopperCreditCard.VISA_CREDIT_CARD;
     *
     * @throws InterruptedException
     * @throws JSONException
     * @throws BSPaymentRequestException
     */
    @Test
    public void full_billing_with_email_with_shipping_create_payment_flow() throws InterruptedException, JSONException, BSPaymentRequestException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(true, false, true);
        createPaymentSetup(RETURNING_SHOPPER_FULL_BILLING_WITH_SHIPPING_CREDIT_CARD);

        uIAutoTestingBlueSnapService.makeCreatePaymentTransaction();
        Assert.assertEquals("wrong credit card was charged", uIAutoTestingBlueSnapService.getTransactions().getCardLastFourDigits(), TestingShopperCreditCard.VISA_CREDIT_CARD.getCardLastFourDigits());
    }

    @Test
    public void paypal_create_payment_flow() throws InterruptedException, JSONException, BSPaymentRequestException {
        shopperCheckoutRequirements = new TestingShopperCheckoutRequirements(false, false, false);
        createPaymentSetup(RETURNING_SHOPPER_PAY_PAL);

        //wait for web to load
        sleep(20000);

        payPalWebViewTests.payPalBasicTransaction(false, checkoutCurrency, purchaseAmount);
    }
}
