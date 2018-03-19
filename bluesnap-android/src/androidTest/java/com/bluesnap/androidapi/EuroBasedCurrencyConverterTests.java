package com.bluesnap.androidapi;

import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.bluesnap.androidapi.models.PriceDetails;
import com.bluesnap.androidapi.models.SdkRequest;
import com.bluesnap.androidapi.services.BSPaymentRequestException;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Created by oz on 4/4/16.
 */
@RunWith(AndroidJUnit4.class)
public class EuroBasedCurrencyConverterTests extends BSAndroidTestsBase {
    private static final String TAG = EuroBasedCurrencyConverterTests.class.getSimpleName();
    private static final String EUR = "EUR";
    private static final String ILS = "ILS";
    private static final String USD = "USD";

    public EuroBasedCurrencyConverterTests() {
        super(EUR);
    }

    @After
    public void keepRunning() throws InterruptedException {
        Thread.sleep(1000);
    }


    @Before
    public void setup() throws InterruptedException {
        super.getToken();
        Log.i(TAG, "=============== Starting rates service tests ==================");
    }


    @Test
    public void convert_EUR_to_USD() throws InterruptedException, BSPaymentRequestException {

        Double amount = 10D;
        SdkRequest sdkRequest = new SdkRequest(amount, EUR, 0d, false, false, false);

        blueSnapService.setSdkRequest(sdkRequest);
        PriceDetails priceDetails = sdkRequest.getPriceDetails();
        blueSnapService.convertPrice(priceDetails, USD);
        Double convertedOncePrice = priceDetails.getAmount();
//        assertEquals("14.42", new BigDecimal(convertedOncePrice).setScale(2, RoundingMode.HALF_UP).toString());
        assertEquals("14.42", String.format("%.2f", convertedOncePrice));
    }

    @Test
    public void convert_EUR_to_ILS_to_USD() throws InterruptedException, BSPaymentRequestException {

        Double amount = 10.7D;
        SdkRequest sdkRequest = new SdkRequest(amount, EUR, 0d, false, false, false);

        blueSnapService.setSdkRequest(sdkRequest);
        PriceDetails priceDetails = sdkRequest.getPriceDetails();
        blueSnapService.convertPrice(priceDetails, ILS);
        Double convertedOncePrice = priceDetails.getAmount();
        blueSnapService.convertPrice(priceDetails, USD);
        Double convertedTwicePrice = priceDetails.getAmount();
        //assertEquals("14.42", new BigDecimal(convertedOncePrice).setScale(2, RoundingMode.HALF_UP).toString());
        assertEquals("15.43", String.format("%.2f", convertedTwicePrice));
    }


    @Test
    public void non_existing_currency_code() throws InterruptedException {

        Double amount = 30.5D;
        SdkRequest sdkRequest = new SdkRequest(amount, "SOMETHING_BAD", 0d, false, false, false);

        try {
            blueSnapService.setSdkRequest(sdkRequest);
            PriceDetails priceDetails = sdkRequest.getPriceDetails();
            blueSnapService.convertPrice(priceDetails, ILS);
            Double ILSPrice = priceDetails.getAmount();
            fail("Should have trown exception");
        } catch (BSPaymentRequestException e) {
            assertEquals("null sdkRequest", e.getMessage());
        } catch (IllegalArgumentException e) {
            assertEquals("not an ISO 4217 compatible 3 letter currency representation", e.getMessage());
        }

    }

    @Test
    public void null_currency_code() throws InterruptedException {

        Double amount = 30.5D;

        SdkRequest sdkRequest = new SdkRequest(amount, "USD", 0d, false, false, false);

        try {
            blueSnapService.setSdkRequest(sdkRequest);
            PriceDetails priceDetails = sdkRequest.getPriceDetails();
            blueSnapService.convertPrice(priceDetails, "SOMETHING_BAD");
            fail("Should have trown exception");
        } catch (BSPaymentRequestException e) {
            assertEquals("null sdkRequest", e.getMessage());
        } catch (IllegalArgumentException e) {
            assertEquals("not an ISO 4217 compatible 3 letter currency representation", e.getMessage());
        }

    }
}