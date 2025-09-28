package com.bluesnap.androidapi.services

import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import com.bluesnap.androidapi.http.AppExecutors
import com.bluesnap.androidapi.http.BlueSnapHTTPResponse
import com.bluesnap.androidapi.models.BSTokenizeDetailsJsonFactory
import com.bluesnap.androidapi.models.BSTokenizeEcpAchDetailsJsonFactory
import com.bluesnap.androidapi.models.BillingContactInfo
import com.bluesnap.androidapi.models.ChosenPaymentMethod
import com.bluesnap.androidapi.models.CreditCard
import com.bluesnap.androidapi.models.Currency
import com.bluesnap.androidapi.models.EcpAchDetails
import com.bluesnap.androidapi.models.PriceDetails
import com.bluesnap.androidapi.models.PurchaseDetails
import com.bluesnap.androidapi.models.SDKConfiguration
import com.bluesnap.androidapi.models.SdkRequest
import com.bluesnap.androidapi.models.SdkRequestBase
import com.bluesnap.androidapi.models.SdkResult
import com.bluesnap.androidapi.models.ShippingContactInfo
import com.bluesnap.androidapi.models.Shopper
import com.bluesnap.androidapi.models.ShopperConfiguration
import com.bluesnap.androidapi.models.SupportedPaymentMethods
import com.bluesnap.androidapi.utils.JsonParser
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.util.Locale

/**
 * Core BlueSnap Service class that handles network and maintains [SdkRequest]
 */
class BlueSnapService {
    private val blueSnapAPI = BlueSnapAPI.instance
    private val kountService = KountService.instance
    private var sdkResult: SdkResult? = null
    private var sdkRequestBase: SdkRequestBase? = null
    var blueSnapToken: BluesnapToken? = null
        private set
    private var bluesnapServiceCallback: BluesnapServiceCallback? = null
    private var sDKConfiguration: SDKConfiguration? = null
    var tokenProvider: TokenProvider? = null
        private set

    @get:Synchronized
    var appExecutors: AppExecutors? = null
        get() {
            if (field == null) {
                field = AppExecutors()
            }
            return field
        }
        private set

    fun getsDKConfiguration(): SDKConfiguration? {
        return sDKConfiguration
    }

    fun isexpressCheckoutActive(): Boolean {
        return sDKConfiguration?.supportedPaymentMethods?.isPaymentMethodActive(
            SupportedPaymentMethods.PAYPAL
        ) ?: false
    }

    fun clearPayPalToken() {
        payPalToken = ""
    }

    val transactionStatus: String?
        get() = Companion.transactionStatus

    /**
     * Setup the service to talk to the server.
     * This will reset the previous payment request
     *
     * @param merchantToken A Merchant SDK token, obtained from the merchant.
     * @param tokenProvider A merchant function for requesting a new token if expired
     * merchantStoreCurrency = USD
     * @param context       A Merchant Application Context
     * @param callback      A [BluesnapServiceCallback]
     */
    fun setup(
        merchantToken: String,
        tokenProvider: TokenProvider?,
        context: Context,
        callback: BluesnapServiceCallback?
    ) {
        setup(merchantToken, tokenProvider, SupportedPaymentMethods.USD, context, callback)
    }

    /**
     * Setup the service to talk to the server.
     * This will reset the previous payment request
     *
     * @param merchantToken         A Merchant SDK token, obtained from the merchant.
     * @param tokenProvider         A merchant function for requesting a new token if expired
     * @param merchantStoreCurrency A Merchant base currency, obtained from the merchant.
     * @param context               A Merchant Application Context
     * @param callback              A [BluesnapServiceCallback]
     */
    fun setup(
        merchantToken: String,
        tokenProvider: TokenProvider?,
        merchantStoreCurrency: String,
        context: Context,
        callback: BluesnapServiceCallback?
    ) {
        bluesnapServiceCallback = callback
        if (null != tokenProvider) this.tokenProvider = tokenProvider
        blueSnapToken = BluesnapToken(merchantToken, tokenProvider)
        blueSnapToken?.let {
            blueSnapAPI.setupMerchantToken(it.merchantToken, it.url)
        }
        sdkResult = null
        clearPayPalToken()
        sdkInit(merchantStoreCurrency, context, callback)
        Log.d(TAG, "Service setup with token" + merchantToken.substring(merchantToken.length - 5))
    }

    /**
     * check if paypal url is same as before and clears it if so
     *
     * @param merchantToken
     */
    private fun initPayPal(merchantToken: String) {
        if (merchantToken != blueSnapToken?.merchantToken && "" != payPalToken) {
            Log.d(TAG, "clearPayPalToken")
            clearPayPalToken()
        } else {
            Log.d(TAG, "PayPal token reuse")
        }
    }

    /**
     * Change the token after expiration occurred.
     *
     * @param merchantToken A Merchant SDK token, obtained from the merchant.
     */
    private fun changeExpiredToken(merchantToken: String) {
        blueSnapToken = BluesnapToken(merchantToken, tokenProvider)
        blueSnapToken?.setToken(merchantToken)
        initPayPal(merchantToken)
        blueSnapToken?.let {
            blueSnapAPI.setupMerchantToken(it.merchantToken, it.url)
        }

        // after expired token is replaced - placing new token in payment result
        if (null != sdkResult) sdkResult?.token = merchantToken
    }

    fun setNewToken(newToken: String) {
        changeExpiredToken(newToken)
    }

    /**
     * Update shopper details on the BlueSnap Server
     *
     * @param shopper  - [Shopper]
     * @param callback - [BluesnapServiceCallback]
     */
    fun submitUpdatedShopperDetails(shopper: Shopper, callback: BluesnapServiceCallback) {
        Log.d(TAG, "update Shopper on token " + blueSnapToken.toString())
        appExecutors?.networkIO()?.execute {
            val response = blueSnapAPI.updateShopper(shopper.toJson().toString())
            if (response.responseCode == HttpURLConnection.HTTP_OK) {
                callback.onSuccess()
            } else if (HttpURLConnection.HTTP_UNAUTHORIZED == response.responseCode) {
                Log.e(
                    TAG,
                    "create PayPal Token service error"
                )
                tokenExpiredAction(
                    callback,
                    object :
                        AfterNewTokenCreatedAction {
                        override fun complete() {
                            submitUpdatedShopperDetails(shopper, callback)
                        }
                    })
            } else {
                val errorMsg = String.format(
                    "submit Updated Shopper Details error [%s], [%s]",
                    response.responseCode,
                    response.responseString
                )
                Log.e(
                    TAG,
                    errorMsg
                )
                callback.onFailure()
            }
        }
    }

    /**
     * check Credit Card Number In Server
     *
     * @param creditCardNumber - credit Card Number String [CreditCard]
     * @throws JSONException                in case of invalid JSON object (should not happen)
     * @throws UnsupportedEncodingException should not happen
     */
    @Throws(JSONException::class, UnsupportedEncodingException::class)
    fun submitTokenizedCCNumber(creditCardNumber: String?): BlueSnapHTTPResponse {
        Log.d(TAG, "Tokenizing card on token " + blueSnapToken.toString())
        val postData = JSONObject()
        postData.put(BSTokenizeDetailsJsonFactory.CCNUMBER, creditCardNumber)
        return blueSnapAPI.tokenizeDetails(postData.toString())
    }

    /**
     * Submit GPay result token to server
     *
     * @param paymentToken - payment token (for GPay, this is a base64-encoded payload data)
     * @param paymentMethod - payment method (for example: SupportedPaymentMethods.GOOGLE_PAY)
     * @throws JSONException                in case of invalid JSON object (should not happen)
     * @throws UnsupportedEncodingException should not happen
     */
    @Throws(JSONException::class, UnsupportedEncodingException::class)
    fun submitTokenenizedPayment(
        paymentToken: String?,
        paymentMethod: String?
    ): BlueSnapHTTPResponse {
        Log.d(TAG, "Tokenizing GPay on token " + blueSnapToken.toString())
        val postData = JSONObject()
        postData.put(BSTokenizeDetailsJsonFactory.PAYMENT_TOKEN, paymentToken)
        postData.put(BSTokenizeDetailsJsonFactory.PAYMENT_METHOD, paymentMethod)
        JsonParser.putJSONifNotNull(
            postData,
            BSTokenizeDetailsJsonFactory.FRAUDSESSIONID,
            kountService.kountSessionId
        )
        return blueSnapAPI.tokenizeDetails(postData.toString())
    }

    /**
     * Update details on the BlueSnapValidator Server
     *
     * @param purchaseDetails [PurchaseDetails]
     * @throws JSONException                in case of invalid JSON object (should not happen)
     * @throws UnsupportedEncodingException should not happen
     */
    @Throws(JSONException::class)
    fun submitTokenizedDetails(purchaseDetails: PurchaseDetails?): BlueSnapHTTPResponse {
        Log.d(TAG, "Tokenizing card on token " + blueSnapToken.toString())
        return blueSnapAPI.tokenizeDetails(
            BSTokenizeDetailsJsonFactory.createDataObject(
                purchaseDetails!!
            ).toString()
        )
    }


    /**
     * Tokenize ACH/ECP details,
     * blueSnapHTTPResponse.getResponseCode() will return HTTP_OK on successful tokenization.
     * @return BlueSnapHTTPResponse
     * @param ecpAchDetails [EcpAchDetails]
     * @throws JSONException                in case of invalid JSON object
     */
    @Throws(JSONException::class)
    fun submitTokenizedEcpAchDetails(ecpAchDetails: EcpAchDetails): BlueSnapHTTPResponse {
        Log.d(TAG, "Tokenizing ECP details on token " + blueSnapToken.toString())
        return blueSnapAPI.tokenizeDetails(
            BSTokenizeEcpAchDetailsJsonFactory.createDataObject(
                ecpAchDetails
            ).toString()
        )
    }

    /**
     * Update details on the BlueSnapValidator Server
     *
     * @param creditCard [CreditCard]
     */
    fun submitCreditCardDetailsForShopperConfiguration(creditCard: CreditCard): BlueSnapHTTPResponse {
        Log.d(TAG, "Tokenizing card on token " + blueSnapToken.toString())
        val jsonObject = JSONObject()
        JsonParser.putJSONifNotNull(
            jsonObject,
            BSTokenizeDetailsJsonFactory.CARDTYPE,
            creditCard.cardType
        )
        JsonParser.putJSONifNotNull(
            jsonObject,
            BSTokenizeDetailsJsonFactory.LAST4DIGITS,
            creditCard.cardLastFourDigits
        )
        JsonParser.putJSONifNotNull(
            jsonObject,
            BSTokenizeDetailsJsonFactory.FRAUDSESSIONID,
            kountService.kountSessionId
        )
        return blueSnapAPI.tokenizeDetails(jsonObject.toString())
    }

    /**
     * Check if Token is Expired on the BlueSnap Server
     * need to be empty JSON otherwise will receive general server error
     *
     * @throws JSONException                in case of invalid JSON object (should not happen)
     * @throws UnsupportedEncodingException should not happen
     */
    @Throws(UnsupportedEncodingException::class)
    private fun checkTokenIsExpired(): BlueSnapHTTPResponse {
        Log.d(TAG, "Check if Token is Expired: " + blueSnapToken.toString())
        return blueSnapAPI.tokenizeDetails(JSONObject().toString())
    }

    /**
     * check Token Is Expired and tries to create a new one if so
     *
     * @param callback                   - [BluesnapServiceCallback]
     * @param afterNewTokenCreatedAction - [AfterNewTokenCreatedAction]
     */
    private fun tokenExpiredAction(
        callback: BluesnapServiceCallback?,
        afterNewTokenCreatedAction: AfterNewTokenCreatedAction
    ) {
        // try to PUT empty {} to check if token is expired
        appExecutors?.networkIO()?.execute {
            try {
                val response = checkTokenIsExpired()
                if (response.responseCode == HttpURLConnection.HTTP_OK) {
                    Log.e(TAG, "SDK Init service error, checkTokenIsExpired successful")
                    callback?.onFailure()
                } else if (response.responseCode == HttpURLConnection.HTTP_BAD_REQUEST && null != tokenProvider && "" != response.errorResponseString) {
                    try {

                        if(response.errorResponseString == "\"EXPIRED_TOKEN\""){
                            tokenProvider?.getNewToken(
                                TokenServiceCallback { newToken ->
                                    setNewToken(newToken)
                                    afterNewTokenCreatedAction.complete()
                                }
                            )
                            return@execute
                        }
                        val errorResponse = JSONObject(response.errorResponseString)
                        val rs2 = errorResponse["message"] as JSONArray
                        val rs3 = rs2[0] as JSONObject
                        if ("EXPIRED_TOKEN" == rs3["errorName"]) {
                            tokenProvider?.getNewToken(
                                TokenServiceCallback { newToken ->
                                    setNewToken(newToken)
                                    afterNewTokenCreatedAction.complete()
                                }
                            )
                        } else {
                            Log.e(TAG, "Token not found error")
                            callback?.onFailure()
                        }
                    } catch (e: JSONException) {
                        Log.e(TAG, "json parsing exception", e)
                        callback?.onFailure()
                    }
                } else {
                    val errorMsg = String.format(
                        "Service Error for tokenExpiredAction [%s], [%s]",
                        response.responseCode,
                        response.responseString
                    )
                    Log.e(TAG, errorMsg)
                    callback?.onFailure()
                }
            } catch (ex: UnsupportedEncodingException) {
                ex.message?.let {
                    Log.e(
                        TAG,
                        it
                    )
                }
                callback?.onFailure()
            }
        }
    }

    /**
     * SDK Init.
     *
     * @param merchantStoreCurrency All rates are derived from merchantStoreCurrency. merchantStoreCurrency * AnyRate = AnyCurrency
     */
    private fun sdkInit(
        merchantStoreCurrency: String,
        context: Context?,
        callback: BluesnapServiceCallback?
    ) {
        appExecutors?.networkIO()?.execute {
            val response = blueSnapAPI.sdkInit(merchantStoreCurrency)
            if (response.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    sDKConfiguration = JsonParser.parseSdkConfiguration(response.responseString)
                    //sDKConfiguration.getRates().setInitialRates();
                    try {
                        blueSnapToken?.let {
                            if (context != null) kountService.setupKount(
                                sDKConfiguration?.kountMerchantId,
                                context,
                                it.isProduction
                            )
                        }

                    } catch (e: Exception) {
                        Log.e(TAG, "Kount SDK initialization error " + e.message)
                    }
                    val cardinalManager = CardinalManager.getInstance()

                    sDKConfiguration?.let {
                        //set JWT in Cardinal manager
                        cardinalManager.setCardinalJWT(it.cardinalToken)
                    }


                    blueSnapToken?.let {
                        cardinalManager.configureCardinal(context, it.isProduction)
                    }
                    //cardinal configure & init
                    cardinalManager.initCardinal { callback?.onSuccess() }
                } catch (e: Exception) {
                    Log.e(TAG, "exception: ", e)
                    callback?.onFailure()
                }
            } else if (HttpURLConnection.HTTP_UNAUTHORIZED == response.responseCode) {
                Log.e(TAG, "SDK Init service error")
                tokenExpiredAction(callback, object : AfterNewTokenCreatedAction {
                    override fun complete() {
                        sdkInit(merchantStoreCurrency, context, bluesnapServiceCallback)
                    }
                })
            } else {
                val errorMsg = String.format(
                    "SDK Init service error [%s], [%s]",
                    response.responseCode,
                    response.errorResponseString
                )
                Log.e(TAG, errorMsg)
                callback?.onFailure()
            }
        }
    }

    val ratesArray: ArrayList<Currency>?
        /**
         * retrieve Rates Array
         *
         * @return Currency Rate Array
         */
        get() = sDKConfiguration?.rates?.currencies

    /**
     * activates creation of PayPal Token(URL) [BlueSnapAPI]
     *
     * @param amount   - amount to change
     * @param currency - currency to charge with
     * @param callback - what to do when done
     */
    fun createPayPalToken(amount: Double?, currency: String?, callback: BluesnapServiceCallback) {
        appExecutors?.networkIO()?.execute(Runnable {
            val response = blueSnapAPI.createPayPalToken(
                amount,
                currency,
                sdkRequestBase?.shopperCheckoutRequirements?.isShippingRequired ?: false
            )
            if (response.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    payPalToken = JSONObject(response.responseString).getString("paypalUrl")
                    callback.onSuccess()
                } catch (e: JSONException) {
                    Log.e(TAG, "json parsing exception", e)
                    errorDescription = JSONObject()
                    appExecutors?.mainThread()?.execute(Runnable {
                        Log.e(TAG, "paypal call return bad response:" + response.responseCode)
                        callback.onFailure()
                    })
                }
            } else if (HttpURLConnection.HTTP_UNAUTHORIZED == response.responseCode) {
                Log.e(TAG, "create PayPal Token service error")
                tokenExpiredAction(callback, object : AfterNewTokenCreatedAction {
                    override fun complete() {
                        createPayPalToken(amount, currency, callback)
                    }
                })
            } else if ((HttpURLConnection.HTTP_BAD_REQUEST == response.responseCode
                        || HttpURLConnection.HTTP_FORBIDDEN == response.responseCode)
                && response.errorResponseString != null
            ) {
                errorDescription = JSONObject()
                try {
                    val errorResponseJSONArray =
                        JSONObject(response.errorResponseString).getJSONArray("message")
                    val errorJson = errorResponseJSONArray.getJSONObject(0)
                    errorDescription = errorJson
                } catch (e: JSONException) {
                    Log.e(TAG, "json parsing exception", e)
                }
                appExecutors?.mainThread()?.execute(Runnable {
                    Log.e(TAG, "paypal call return bad response:" + response.responseCode)
                    callback.onFailure()
                })
            } else {
                val errorMsg = String.format(
                    "create PayPal Token service error [%s], [%s]",
                    response.responseCode,
                    response.responseString
                )
                Log.e(TAG, errorMsg)
                callback.onFailure()
            }
        })
    }

    /**
     * check transaction status after PayPal transaction occurred
     *
     * @param callback - what to do when done
     */
    fun retrieveTransactionStatus(callback: BluesnapServiceCallback) {
        appExecutors?.networkIO()?.execute {
            val response = blueSnapAPI.retrieveTransactionStatus()
            if (response.responseCode == HttpURLConnection.HTTP_OK) {
                try {
                    Companion.transactionStatus =
                        JSONObject(response.responseString).getString("processingStatus")
                    callback.onSuccess()
                } catch (e: JSONException) {
                    Log.e(
                        TAG,
                        "json parsing exception",
                        e
                    )
                    callback.onFailure()
                }
            } else {
                // if token is expired than transaction will fail
                Log.e(
                    TAG,
                    "PayPal service error"
                )
                callback.onFailure()
            }
        }
    }

    val supportedRates: Set<String>?
        /**
         * Get a set of strings of the supported conversion rates
         *
         * @return [&lt; String &gt;][Set]
         */
        get() = sDKConfiguration?.rates?.currencyCodes

    /**
     * check currency received from merchant and verify it actually exists
     *
     * @param currencyNameCode ISO 4217 compatible  3 letter currency representation
     * @return boolean
     */
    private fun checkCurrencyCompatibility(currencyNameCode: String): Boolean {
        return null != supportedRates && supportedRates!!.contains(currencyNameCode)
    }

    /**
     * Convert a price in USD to a price in another currency  in ISO 4217 Code.
     * Before
     *
     * @param usdPrice  A String representation of a USD price which will be converted to a double value.
     * @param convertTo ISO 4217 compatible  3 letter currency representation
     * @return String representation of converted price.
     */
    fun convertUSD(usdPrice: String?, convertTo: String): String {
        if (usdPrice == null || usdPrice.isEmpty()) return "0"
        val priceDetails =
            PriceDetails(java.lang.Double.valueOf(usdPrice), SupportedPaymentMethods.USD, 0.0)
        convertPrice(priceDetails, convertTo)
        return AndroidUtil.getDecimalFormat().format(priceDetails.amount).toString()
    }

    /**
     * Convert a price in currentCurrencyNameCode to newCurrencyNameCode locally and return it
     *
     * @param receivedPriceDetails The price details before conversion
     * @param newCurrencyCode      The ISO 4217 currency name
     * @return priceDetails [PriceDetails]
     */
    fun getConvertedPriceDetails(
        receivedPriceDetails: PriceDetails,
        newCurrencyCode: String?
    ): PriceDetails {
        val localPriceDetails = PriceDetails(
            receivedPriceDetails.subtotalAmount,
            receivedPriceDetails.currencyCode,
            receivedPriceDetails.taxAmount
        )
        val currentCurrencyCode = localPriceDetails.currencyCode
        require(
            currentCurrencyCode  != null && newCurrencyCode != null &&  !(!checkCurrencyCompatibility(currentCurrencyCode) || !checkCurrencyCompatibility(
                newCurrencyCode
            ))
        ) { "not an ISO 4217 compatible 3 letter currency representation" }

        // get Rates
        val rates = sDKConfiguration!!.rates

        // check if currentCurrencyNameCode is MerchantStoreCurrency
        val currentRate = rates.getCurrencyByCode(currentCurrencyCode).conversionRate
        val newRate = rates.getCurrencyByCode(newCurrencyCode).conversionRate / currentRate
        val newSubtotal = localPriceDetails.subtotalAmount * newRate
        val taxAmount = localPriceDetails.taxAmount
        val newTaxAmount = if (taxAmount == null) null else taxAmount * newRate
        localPriceDetails[newSubtotal, newCurrencyCode] = newTaxAmount
        return localPriceDetails
    }

    /**
     * Convert a price in currentCurrencyNameCode to newCurrencyNameCode
     *
     * @param priceDetails    The price details before conversion
     * @param newCurrencyCode The ISO 4217 currency name
     */
    fun convertPrice(priceDetails: PriceDetails, newCurrencyCode: String?) {
        val localPriceDetails = getConvertedPriceDetails(priceDetails, newCurrencyCode)
        priceDetails[localPriceDetails.subtotalAmount, localPriceDetails.currencyCode] =
            localPriceDetails.taxAmount
    }

    /**
     * get SdkResult with token, amount and currency set
     *
     * @return [SdkResult]
     */
    @Synchronized
    fun getSdkResult(): SdkResult {
        var result = sdkResult
        if (result == null) {
            result = SdkResult()
        }

        sdkResult = result

        try {
            result.token = blueSnapToken?.merchantToken
            sdkRequestBase?.setSdkResult(result)
        } catch (e: Exception) {
            Log.e(TAG, "sdkResult set Token, Amount, Currency or ShopperId resulted in an error")
        }
        return result
    }

    @set:Throws(BSPaymentRequestException::class)
    @set:Synchronized
    var sdkRequest: SdkRequestBase?
        get() = sdkRequestBase
        /**
         * Set a sdkRequest and call on  it.
         *
         * @param newSdkRequestBase SdkRequestBase an Sdk request to uses
         * @throws BSPaymentRequestException in case of invalid SdkRequest
         */
        set(newSdkRequestBase) {
            if (sdkRequestBase != null) {
                Log.w(TAG, "sdkRequest override")
            }
            sdkRequestBase = newSdkRequestBase
            sdkResult = SdkResult()
            // Copy values from request
            sdkRequestBase?.setSdkResult(sdkResult)
        }

    /**
     * @param newCurrencyNameCode - String, new Currency Name Code
     * @param context             - Context
     */
    fun onCurrencyChange(newCurrencyNameCode: String, context: Context?) {
        if (sdkRequestBase is SdkRequest) {
            Log.d(
                TAG,
                "onCurrencyChange= $newCurrencyNameCode"
            )

            sdkRequestBase?.let {
                val priceDetails = it.priceDetails
                convertPrice(priceDetails, newCurrencyNameCode)
                sdkResult?.amount = priceDetails.amount
                sdkResult?.currencyNameCode = priceDetails.currencyCode
                // any changes like currency and/or amount while not creating a new token should clear previous used PayPal token
                clearPayPalToken()
                BlueSnapLocalBroadcastManager.sendMessage(
                    context,
                    BlueSnapLocalBroadcastManager.CURRENCY_UPDATED_EVENT,
                    TAG
                )
            }

        }
    }

    /**
     * check if country has zip according to countries without zip array [Constants]
     *
     * @param context - [Context]
     * @return boolean
     */
    fun doesCountryhaveZip(context: Context): Boolean {
        return BlueSnapValidator.instance.checkCountryHasZip(getUserCountry(context))
    }

    /**
     * returns user country according to [TelephonyManager] sim or network
     *
     * @param context - [Context]
     * @return Country - ISO 3166-1 alpha-2 standard, default value is US
     */
    fun getUserCountry(context: Context): String {
        try {
            val tm = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val simCountry = tm.simCountryIso
            if (simCountry != null && simCountry.length == 2) {
                return simCountry.uppercase()
            } else if (tm.phoneType != TelephonyManager.PHONE_TYPE_CDMA) {
                val networkCountry = tm.networkCountryIso
                if (networkCountry != null && networkCountry.length == 2) {
                    return networkCountry.uppercase()
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "TelephonyManager, getSimCountryIso or getNetworkCountryIso failed")
        }
        return Locale.US.country
    }

    /**
     * Update the roce details according to shipping country and state, by calling the provided TaxCalculator.
     *
     * @param shippingCountry
     * @param shippingState
     * @param context
     */
    fun updateTax(shippingCountry: String?, shippingState: String?, context: Context?) {
        sdkRequestBase?.updateTax(shippingCountry, shippingState)
        // send event to update amount in UI
        if (sdkRequestBase is SdkRequest) BlueSnapLocalBroadcastManager.sendMessage(
            context,
            BlueSnapLocalBroadcastManager.CURRENCY_UPDATED_EVENT,
            TAG
        )
    }

    val shopperConfiguration: ShopperConfiguration?
        /**
         * After calling initBluesnap() with a token created for an existing shopper, the merchant app can use this method to
         * get the shopper details, including the chosen payment method
         *
         * @return ShopperConfiguration
         */
        get() {
            var res: ShopperConfiguration? = null
            val shopper = sDKConfiguration?.shopper
            if (shopper != null) {
                val billingContactInfo = BillingContactInfo(shopper)
                billingContactInfo.email = shopper.email
                val shippingContactInfo =
                    if (shopper.shippingContactInfo == null) null else ShippingContactInfo(shopper.shippingContactInfo)
                val chosenPaymentMethod =
                    if (shopper.chosenPaymentMethod == null) null else ChosenPaymentMethod(shopper.chosenPaymentMethod)
                res = ShopperConfiguration(
                    billingContactInfo,
                    shippingContactInfo,
                    chosenPaymentMethod
                )
            }
            return res
        }

    private interface AfterNewTokenCreatedAction {
        fun complete()
    }

    companion object {
        private val TAG = BlueSnapService::class.java.simpleName
        val instance = BlueSnapService()
        var payPalToken: String? = null
            private set
        var errorDescription: JSONObject? = null
            private set
        private var transactionStatus: String? = null
    }
}