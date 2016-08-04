package com.bluesnap.android.demoapp;

import android.content.Context;
import android.util.Log;

import com.bluesnap.androidapi.services.BluesnapServiceCallback;
import com.bluesnap.androidapi.services.PrefsStorage;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.TextHttpResponseHandler;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.StringEntity;

/**
 * Created by roy.biber on 04/08/2016.
 */
public class Transactions {

    private static final String TAG = Transactions.class.getSimpleName();
    private static final Transactions INSTANCE = new Transactions();
    private String SHOPPER_ID = "SHOPPER_ID";
    private String message;
    private String title;
    private Context context;

    public static Transactions getInstance() {
        return INSTANCE;
    }

    public void createCreditCardTransaction(String firstName, String lastName, String token, String currency, Double amount, final BluesnapServiceCallback callback) {
        createCreditCardTransaction(firstName, lastName, token, currency, amount, false, "", "", callback);
    }

    public void createCreditCardTransaction(String firstName, String lastName, String token, String currency, Double amount, boolean isReturningShopper, String last4Digits, String cardType, final BluesnapServiceCallback callback) {

        String bodyStart = "<card-transaction xmlns=\"http://ws.plimus.com\">" +
                "<card-transaction-type>AUTH_CAPTURE</card-transaction-type>" +
                "<recurring-transaction>ECOMMERCE</recurring-transaction>" +
                "<soft-descriptor>MobileSDK</soft-descriptor>" +
                "<amount>" + amount + "</amount>" +
                "<currency>" + currency + "</currency>";
        String bodyMiddle = "<card-holder-info>" +
                "<first-name>" + firstName + "</first-name>" +
                "<last-name>" + lastName + "</last-name>" +
                "</card-holder-info>";
        String bodyEnd = "</card-transaction>";
        String body = bodyStart;

        if (!isReturningShopper) {
            body += bodyMiddle +
                    "<pf-token>" + token + "</pf-token>" +
                    bodyEnd;
        } else if (!"".equals(getShopperId())) {
            body += "<vaulted-shopper-id>" + getShopperId() + "</vaulted-shopper-id>" +
                    bodyMiddle +
                    " <credit-card>" +
                    "<card-last-four-digits>" + last4Digits + "</card-last-four-digits>" +
                    "<card-type>" + cardType.toUpperCase() + "</card-type>" +
                    "</credit-card>" +
                    bodyEnd;
        }

        StringEntity entity = new StringEntity(body, "UTF-8");
        AsyncHttpClient httpClient = new AsyncHttpClient();
        httpClient.setBasicAuth("GCpapi", "Plimus4321");
        Log.d(TAG, "Create transaction body:\n" + body);
        httpClient.post(getContext(), "https://us-qa-fct03.bluesnap.com/services/2/transactions", entity, "application/xml", new TextHttpResponseHandler() {
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.e(TAG, responseString, throwable);
                //Disabled until server will return a reasonable error
                String errorName = "Error Server";
                try {
                    if (responseString != null)
                        errorName = responseString.substring(responseString.indexOf("<error-name>") + "<error-name>".length(), responseString.indexOf("</error-name>"));
                } catch (Exception e) {
                    Log.w(TAG, "failed to get error name from response string");
                }
                setMessage(errorName);
                setTitle("Merchant Server");
                callback.onFailure();
            }

            @Override
            public void onSuccess(int statusCode, Header[] headers, String responseString) {
                setShopperId(responseString.substring(responseString.indexOf("<vaulted-shopper-id>") +
                        "<vaulted-shopper-id>".length(), responseString.indexOf("</vaulted-shopper-id>")));
                Log.d(TAG, responseString);
                setMessage("Transaction Success " + getShopperId());
                setTitle("Merchant Server");
                callback.onSuccess();
            }
        });
    }

    private String getShopperId() {
        PrefsStorage prefsStorage = new PrefsStorage(getContext());
        return prefsStorage.getString(SHOPPER_ID, "");
    }

    private void setShopperId(String shopperId) {
        PrefsStorage prefsStorage = new PrefsStorage(getContext());
        prefsStorage.putString(SHOPPER_ID, shopperId);
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

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }
}
