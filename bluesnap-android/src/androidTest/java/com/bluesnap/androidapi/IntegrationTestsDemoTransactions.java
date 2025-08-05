package com.bluesnap.androidapi;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.bluesnap.androidapi.http.BlueSnapHTTPResponse;
import com.bluesnap.androidapi.http.CustomHTTPParams;
import com.bluesnap.androidapi.http.HTTPOperationController;
import com.bluesnap.androidapi.models.SdkResult;
import com.bluesnap.androidapi.services.BlueSnapService;
import com.bluesnap.androidapi.services.BluesnapServiceCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static com.bluesnap.androidapi.SandboxToken.*;
import static com.bluesnap.androidapi.utils.JsonParser.getOptionalString;
import static java.net.HttpURLConnection.HTTP_OK;

public class IntegrationTestsDemoTransactions {

    private static final String TAG = IntegrationTestsDemoTransactions.class.getSimpleName();
    private static final IntegrationTestsDemoTransactions INSTANCE = new IntegrationTestsDemoTransactions();
    private String SHOPPER_ID = "SHOPPER_ID";
    private String message;
    private String title;
    private Context context;
    private String transactionId;
    private int accountNumber;

    private String accountType;
    private int routingNumber;

    private  int publicAccountNumber;

    private  int publicRoutingNumber;


    private String cardLastFourDigits;
    private String tokenSuffix = "";


    public static IntegrationTestsDemoTransactions getInstance() {
        return INSTANCE;
    }

    public void createCreditCardTransaction(final SdkResult sdkResult, final BluesnapServiceCallback callback) {

        //TODO: I'm just a string but please don't make me look that bad..Use String.format
        String body = "<card-transaction xmlns=\"http://ws.plimus.com\">" +
                "<card-transaction-type>AUTH_CAPTURE</card-transaction-type>" +
                "<recurring-transaction>ECOMMERCE</recurring-transaction>" +
                "<soft-descriptor>MobileSDK</soft-descriptor>" +
                "<amount>" + sdkResult.getAmount() + "</amount>" +
                "<currency>" + sdkResult.getCurrencyNameCode() + "</currency>" +
                "<transaction-fraud-info>" +
                "<fraud-session-id>" + sdkResult.getKountSessionId() + "</fraud-session-id>" +
                "</transaction-fraud-info>" +
                "<pf-token>" + sdkResult.getToken() + "</pf-token>" +
                "</card-transaction>";


        String basicAuth = "Basic " + Base64.encodeToString((SANDBOX_USER + ":" + SANDBOX_PASS).getBytes(StandardCharsets.UTF_8), 0);
        List<CustomHTTPParams> headerParams = new ArrayList<>();
        headerParams.add(new CustomHTTPParams("Authorization", basicAuth));
        BlueSnapHTTPResponse httpResponse = HTTPOperationController.post(SANDBOX_URL + SANDBOX_CREATE_TRANSACTION, body, "application/xml", "application/xml", headerParams);
        String responseString = httpResponse.getResponseString();
        if (httpResponse.getResponseCode() == HTTP_OK && httpResponse.getHeaders() != null) {
//            setShopperId(responseString.substring(responseString.indexOf("<vaulted-shopper-id>") +
//                    "<vaulted-shopper-id>".length(), responseString.indexOf("</vaulted-shopper-id>")));
            setTransactionId(responseString.substring(responseString.indexOf("<transaction-id>") +
                    "<transaction-id>".length(), responseString.indexOf("</transaction-id>")));
            setCardLastFourDigits(responseString.substring(responseString.indexOf("<card-last-four-digits>") +
                    "<card-last-four-digits>".length(), responseString.indexOf("</card-last-four-digits>")));

            String merchantToken = BlueSnapService.Companion.getInstance().getBlueSnapToken().getMerchantToken();
            setTokenSuffix(merchantToken.substring(merchantToken.length() - 6));
            Log.d(TAG, responseString);
            setMessage("Transaction Success " + getTransactionId());
            setTitle("Merchant Server");
            callback.onSuccess();
        } else {
            Log.e(TAG, responseString);
            //Disabled until server will return a reasonable error
            String errorName = "Transaction Failed";
            try {
                if (responseString != null)
                    errorName = responseString.substring(responseString.indexOf("<error-name>") + "<error-name>".length(), responseString.indexOf("</error-name>"));
                Log.e(TAG, "Failed TX Response:  " + responseString);
            } catch (Exception e) {
                Log.e(TAG, "failed to get error name from response string");
                Log.e(TAG, "Failed TX Response:  " + responseString);
            }
            setMessage(errorName);
            setTitle("Merchant Server");
            callback.onFailure();
        }

    }


    public void createEcpAchTransaction(final SdkResult sdkResult, final BluesnapServiceCallback callback) {

        // Constructing the request body using default data and replacing only pf-token
        String body = String.format(
                "<alt-transaction xmlns=\"http://ws.plimus.com\">" +
                        "<pf-token>%s</pf-token>" +
                        "<soft-descriptor>ABC COMPANY</soft-descriptor>" +
                        "<amount>100.00</amount>" +
                        "<currency>USD</currency>" +
                        "<payer-info>" +
                        "<first-name>John</first-name>" +
                        "<last-name>Doe</last-name>" +
                        "<zip>02453</zip>" +
                        "<phone>1234567890</phone>" +
                        "</payer-info>" +
                        "<authorized-by-shopper>true</authorized-by-shopper>" +
                        "</alt-transaction>",
                sdkResult.getToken()
        );

        String basicAuth = "Basic " + Base64.encodeToString((SANDBOX_USER + ":" + SANDBOX_PASS).getBytes(StandardCharsets.UTF_8), Base64.NO_WRAP);
        List<CustomHTTPParams> headerParams = new ArrayList<>();
        headerParams.add(new CustomHTTPParams("Authorization", basicAuth));
        // Log the curl command for debugging purposes
        String curlCommand = String.format(
                "curl -v -X POST \"%s\" -H \"Authorization: %s\" -H \"Content-Type: application/xml\" -H \"Accept: application/xml\" -d '%s'",
                SANDBOX_URL + SANDBOX_CREATE_TRANSACTION,
                basicAuth,
                body
        );
        Log.d(TAG, "CURL Command: " + curlCommand);
        BlueSnapHTTPResponse httpResponse = HTTPOperationController.post(
                SANDBOX_URL + SANDBOX_CREATE_ALT_TRANSACTION,
                body,
                "application/xml",
                "application/xml",
                headerParams
        );

        String responseString = httpResponse.getResponseString();
        if (httpResponse.getResponseCode() == HTTP_OK && httpResponse.getHeaders() != null) {
            try {
                setTransactionId(responseString.substring(
                        responseString.indexOf("<transaction-id>") + "<transaction-id>".length(),
                        responseString.indexOf("</transaction-id>")
                ));

                setMessage("Transaction Success " + getTransactionId());
                setTitle("Merchant Server");
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse success response", e);
                callback.onFailure();
            }
        } else {
            Log.e(TAG, responseString);
            try {
                String errorName = responseString.substring(
                        responseString.indexOf("<error-name>") + "<error-name>".length(),
                        responseString.indexOf("</error-name>")
                );
                setMessage(errorName);
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse error response", e);
                setMessage("Transaction Failed");
            }
            setTitle("Merchant Server");
            callback.onFailure();
        }
    }

    public String getTransactionId() {
        return transactionId;
    }

    private void setTransactionId(String id) {
        this.transactionId = id;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(int accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public int getRoutingNumber() {
        return routingNumber;
    }

    public void setRoutingNumber(int routingNumber) {
        this.routingNumber = routingNumber;
    }

    public int getPublicAccountNumber() {
        return publicAccountNumber;
    }

    public void setPublicAccountNumber(int publicAccountNumber) {
        this.publicAccountNumber = publicAccountNumber;
    }

    public int getPublicRoutingNumber() {
        return publicRoutingNumber;
    }

    public void setPublicRoutingNumber(int publicRoutingNumber) {
        this.publicRoutingNumber = publicRoutingNumber;
    }



    public String getCardLastFourDigits() {
        return cardLastFourDigits;
    }

    public void setCardLastFourDigits(String cardLastFourDigits) {
        this.cardLastFourDigits = cardLastFourDigits;
    }

    public String getTokenSuffix() {
        return tokenSuffix;
    }

    private void setTokenSuffix(String token) {
        this.tokenSuffix = token;
    }

    public String getMessage() {
        return message;
    }

    private void setMessage(String message) {
        this.message = message;
    }

    public String getTitle() {
        return title;
    }

    private void setTitle(String title) {
        this.title = title;
    }
}
