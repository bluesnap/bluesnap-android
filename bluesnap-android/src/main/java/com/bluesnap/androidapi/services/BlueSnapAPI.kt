package com.bluesnap.androidapi.services

import android.util.Log
import com.bluesnap.androidapi.BuildConfig
import com.bluesnap.androidapi.http.BlueSnapHTTPResponse
import com.bluesnap.androidapi.http.CustomHTTPParams
import com.bluesnap.androidapi.http.HTTPOperationController

/**
 * Created by roy.biber on 14/11/2017.
 */
internal class BlueSnapAPI private constructor() {
    private var merchantToken: String? = null
    private var url: String? = null
    private val headerParams: ArrayList<CustomHTTPParams> = ArrayList()

    /**
     * set BlueSnap API headers and connection setup
     */
    init {
        headerParams.add(
            CustomHTTPParams(
                "BLUESNAP_ORIGIN_HEADER",
                "ANDROID SDK " + BuildConfig.VERSION_CODE
            )
        )
        headerParams.add(
            CustomHTTPParams(
                "BLUESNAP_ORIGIN_VERSION_HEADER",
                BuildConfig.VERSION_NAME
            )
        )
        headerParams.add(
            CustomHTTPParams(
                "BLUESNAP_VERSION_HEADER",
                BLUESNAP_VERSION_HEADER.toString()
            )
        )
    }

    /**
     * tokenize details to server
     *
     * @param body - details to set
     */
    fun tokenizeDetails(body: String?): BlueSnapHTTPResponse {
        Log.d(TAG, "Api request for token detail")
        // headerParams.add(new CustomHTTPParams(TOKEN_AUTHENTICATION, String.valueOf(merchantToken)));
        return HTTPOperationController.put(
            url + CARD_TOKENIZE + merchantToken, body, CONTENT_TYPE,
            ACCEPT, headerParams
        )
    }

    /**
     * update shopper details to server
     *
     * @param body - body string to send to server
     * @return [BlueSnapHTTPResponse]
     */
    fun updateShopper(body: String?): BlueSnapHTTPResponse {
        Log.d(TAG, "Api request for token detail")
        //headerParams.add(new CustomHTTPParams(TOKEN_AUTHENTICATION, String.valueOf(merchantToken)));
        return HTTPOperationController.put(
            url + UPDATE_SHOPPER, body, CONTENT_TYPE,
            ACCEPT, headerParams
        )
    }

    /**
     * add header to http client for TOKEN_AUTHENTICATION and set domain path
     *
     * @param merchantToken
     * @param url
     */
    fun setupMerchantToken(merchantToken: String, url: String?) {
        this.merchantToken = merchantToken
        this.url = url
        headerParams.add(CustomHTTPParams(TOKEN_AUTHENTICATION, merchantToken))
    }

    /**
     * get sdk initilize data from server
     *
     * @param baseCurrency - currency to base the rates on
     */
    fun sdkInit(baseCurrency: String): BlueSnapHTTPResponse {
        return HTTPOperationController.get(
            url + SDK_INIT + BASE_CURRENCY + baseCurrency + CREATE_JWT + "True",
            CONTENT_TYPE,
            ACCEPT,
            headerParams
        )
    }

    /**
     * create PayPal Token (url)
     *
     * @param amount             - amount to charge
     * @param currency           - currency to charge with
     * @param isShippingRequired - boolean is shipping required
     */
    fun createPayPalToken(
        amount: Double?,
        currency: String?,
        isShippingRequired: Boolean
    ): BlueSnapHTTPResponse {
        var urlString = "$url$PAYPAL_SERVICE$amount&currency=$currency"
        if (isShippingRequired) urlString += PAYPAL_SHIPPING
        return HTTPOperationController.get(urlString, CONTENT_TYPE, ACCEPT, headerParams)
    }

    /**
     * check transaction status after PayPal transaction occurred
     */
    fun retrieveTransactionStatus(): BlueSnapHTTPResponse {
        return HTTPOperationController.get(
            url + RETRIEVE_TRANSACTION_SERVICE,
            CONTENT_TYPE,
            ACCEPT,
            headerParams
        )
    }

    /**
     * create Cardinal JWT
     *
     */
    fun createCardinalJWT(): BlueSnapHTTPResponse {
        val urlString = url + CARDINAL_SERVICE_CREATE_JWT
        return HTTPOperationController.post(urlString, null, CONTENT_TYPE, ACCEPT, headerParams)
    }

    /**
     * process Cardinal result
     */
    fun processCardinalResult(body: String?): BlueSnapHTTPResponse {
        val urlString = url + CARDINAL_SERVICE_PROCESS_RESULT
        return HTTPOperationController.post(urlString, body, CONTENT_TYPE, ACCEPT, headerParams)
    }

    companion object {
        private val TAG = BlueSnapAPI::class.java.simpleName
        private const val CONTENT_TYPE = "application/json"
        private const val ACCEPT = "application/json"

        //blueSnap API version
        private const val BLUESNAP_VERSION_HEADER = 2.0
        val instance = BlueSnapAPI()
        const val TOKEN_AUTHENTICATION = "Token-Authentication"
        private const val CARD_TOKENIZE = "payment-fields-tokens/"
        private const val RATES_SERVICE = "tokenized-services/rates"
        private const val BASE_CURRENCY = "?base-currency="
        private const val CREATE_JWT = "&create-jwt="
        private const val SUPPORTED_PAYMENT_METHODS = "tokenized-services/supported-payment-methods"
        private const val SDK_INIT = "tokenized-services/sdk-init"
        private const val UPDATE_SHOPPER = "tokenized-services/shopper"
        private const val PAYPAL_SERVICE = "tokenized-services/paypal-token?amount="
        private const val CARDINAL_SERVICE_CREATE_JWT = "tokenized-services/3ds-jwt"
        private const val CARDINAL_SERVICE_PROCESS_RESULT = "tokenized-services/3ds-process-result"
        private const val PAYPAL_SHIPPING = "&req-confirm-shipping=0&no-shipping=2"
        private const val RETRIEVE_TRANSACTION_SERVICE = "tokenized-services/transaction-status"
    }
}