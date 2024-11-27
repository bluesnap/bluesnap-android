package com.bluesnap.androidapi.services

import android.app.Activity
import android.util.Base64
import android.util.Log
import android.util.Pair
import com.bluesnap.androidapi.models.BillingContactInfo
import com.bluesnap.androidapi.models.CreditCardTypeResolver
import com.bluesnap.androidapi.models.SdkRequestBase
import com.bluesnap.androidapi.models.SdkResult
import com.bluesnap.androidapi.models.ShippingContactInfo
import com.bluesnap.androidapi.models.SupportedPaymentMethods
import com.bluesnap.androidapi.utils.JsonParser
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.identity.intents.model.UserAddress
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.CardRequirements
import com.google.android.gms.wallet.IsReadyToPayRequest
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentMethodTokenizationParameters
import com.google.android.gms.wallet.PaymentsClient
import com.google.android.gms.wallet.TransactionInfo
import com.google.android.gms.wallet.Wallet
import com.google.android.gms.wallet.Wallet.WalletOptions
import com.google.android.gms.wallet.WalletConstants
import org.json.JSONObject
import java.util.Arrays

class GooglePayService {
    private val TAG = GooglePayService::class.java.simpleName

    // Changing this to ENVIRONMENT_PRODUCTION will make the API return real card information.
    // Please refer to the documentation to read about the required steps needed to enable
    // ENVIRONMENT_PRODUCTION.
    //private final int PAYMENTS_ENVIRONMENT = WalletConstants.ENVIRONMENT_PRODUCTION; //.ENVIRONMENT_TEST; //
    // The name of our payment processor / gateway.
    val GATEWAY_TOKENIZATION_NAME = "bluesnap"

    // Currently we support CARD and TOKENIZED CARD (only) for any merchant who supports GOOGLE_PAY
    val SUPPORTED_METHODS =
        Arrays.asList( // PAYMENT_METHOD_CARD returns to any card the user has stored in their Google Account.
            WalletConstants.PAYMENT_METHOD_CARD,  // PAYMENT_METHOD_TOKENIZED_CARD refers to EMV tokenized credentials stored in the
            // Google Pay app, assuming it's installed.
            // Please keep in mind tokenized cards may exist in the Google Pay app without being
            // added to the user's Google Account.
            WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD
        )

    /**
     * In case of success from the Google-Pay button, we create the encoded GPay token we
     * and post it on the HPF
     * Returns the SDK result with the shopper details and the encoded token if all OK, null if not.
     *
     * @param paymentData
     */
    fun createSDKResult(paymentData: PaymentData): SdkResult? {

        // PaymentMethodToken contains the payment information, as well as any additional
        // requested information, such as billing and shipping address.
        // Refer to your processor's documentation on how to proceed from here.
        val token = paymentData.paymentMethodToken

        // getPaymentMethodToken will only return null if PaymentMethodTokenizationParameters was
        // not set in the PaymentRequest. which is never in our case
        var sdkResult: SdkResult? = null
        try {
            val encodedToken = instance.createBlsTokenFromGooglePayPaymentData(paymentData)
            Log.d(TAG, "paymentData encoded as Token for BlueSnap")
            sdkResult = BlueSnapService.instance.getSdkResult()
            sdkResult.chosenPaymentMethodType = SupportedPaymentMethods.GOOGLE_PAY
            sdkResult.googlePayToken = encodedToken
            val billingAddress = paymentData.cardInfo.billingAddress
            if (billingAddress != null) {
                val billingContactInfo = BillingContactInfo()
                billingContactInfo.email = billingAddress.emailAddress
                billingContactInfo.address = billingAddress.address1
                billingContactInfo.address2 = billingAddress.address2
                billingContactInfo.city = billingAddress.locality
                billingContactInfo.country = billingAddress.countryCode
                billingContactInfo.fullName = billingAddress.name
                billingContactInfo.state = billingAddress.administrativeArea
                billingContactInfo.zip = billingAddress.postalCode
                sdkResult.billingContactInfo = billingContactInfo
            }
            val shippingAddress = paymentData.shippingAddress
            if (shippingAddress != null) {
                val shippingContactInfo = ShippingContactInfo()
                shippingContactInfo.phone = shippingAddress.phoneNumber
                shippingContactInfo.address = shippingAddress.address1
                shippingContactInfo.address2 = shippingAddress.address2
                shippingContactInfo.city = shippingAddress.locality
                shippingContactInfo.country = shippingAddress.countryCode
                shippingContactInfo.fullName = shippingAddress.name
                shippingContactInfo.state = shippingAddress.administrativeArea
                shippingContactInfo.zip = shippingAddress.postalCode
                sdkResult.shippingContactInfo = shippingContactInfo
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error encoding payment data into BlueSnap token", e)
        }
        return sdkResult
    }

    /**
     * Creates a base64 encoded token with the PaymentData
     */
    @Throws(Exception::class)
    fun createBlsTokenFromGooglePayPaymentData(paymentData: PaymentData): String {
        val cardInfo = paymentData.cardInfo
        val result = JSONObject()

        // paymentMethodData
        val paymentMethodData = JSONObject()

        // paymentMethodData -> description: A payment method and method identifier suitable for communication to a shopper in a confirmation screen or purchase receipt.
        val description = cardInfo.cardDescription
        if (description != null) {
            paymentMethodData.put("description", description)
        }

        // paymentMethodData -> tokenizationData
        val paymentMethodToken = paymentData.paymentMethodToken
        val tokenizationData = JSONObject()
        tokenizationData.put("type", paymentMethodToken!!.paymentMethodTokenizationType)
        tokenizationData.put("token", paymentMethodToken.token)
        paymentMethodData.put("tokenizationData", tokenizationData)

        // paymentMethodData -> info
        val info = JSONObject()
        paymentMethodData.put("info", info)

        // paymentMethodData -> info -> cardNetwork
        val cardNetwork = cardInfo.cardNetwork
        if (cardNetwork != null) {
            info.put("cardNetwork", cardNetwork)
        }

        // paymentMethodData -> info -> cardDetails
        val cardDetails = cardInfo.cardDetails
        if (cardDetails != null) {
            info.put("cardDetails", cardDetails)
        }

        // paymentMethodData -> info -> cardClass (1-3 or 0, should somehow translate to DEBIT/CREDIT)
        val cardClassCode = cardInfo.cardClass
        var cardClass: String? = null
        if (cardClassCode == WalletConstants.CARD_CLASS_CREDIT) {
            cardClass = "CREDIT"
        } else if (cardClassCode == WalletConstants.CARD_CLASS_DEBIT) {
            cardClass = "DEBIT"
        } else if (cardClassCode == WalletConstants.CARD_CLASS_PREPAID) {
            cardClass = "PREPAID"
        }
        if (cardClass != null) {
            info.put("cardClass", cardClass)
        }
        // paymentMethodData -> info -> billingAddress
        val billingAddressJson = getUserAddressAsJson(cardInfo.billingAddress)
        if (billingAddressJson != null) {
            info.put("billingAddress", billingAddressJson)
        }
        result.put("paymentMethodData", paymentMethodData)

        // email
        val email = paymentData.email
        if (email != null) {
            result.put("email", email)
        }

        // googleTransactionId - not sure this is the right place in the json for it
        val googleTransactionId = paymentData.googleTransactionId
        result.put("googleTransactionId", googleTransactionId)
        // shippingAddress
        val shippingAddressJson =
            getUserAddressAsJson(paymentData.shippingAddress)
        if (shippingAddressJson != null) {
            result.put("shippingAddress", shippingAddressJson)
        }
        val tokenForBls = result.toString()
        return Base64.encodeToString(tokenForBls.toByteArray(), Base64.NO_WRAP or Base64.URL_SAFE)
    }

    @Throws(Exception::class)
    private fun getUserAddressAsJson(userAddress: UserAddress?): JSONObject? {
        var res: JSONObject? = null
        if (userAddress != null) {
            res = JSONObject()
            JsonParser.putJSONifNotNull(res, "name", userAddress.name)
            JsonParser.putJSONifNotNull(res, "postalCode", userAddress.postalCode)
            JsonParser.putJSONifNotNull(res, "countryCode", userAddress.countryCode)
            JsonParser.putJSONifNotNull(res, "phoneNumber", userAddress.phoneNumber)
            JsonParser.putJSONifNotNull(res, "companyName", userAddress.companyName)
            JsonParser.putJSONifNotNull(res, "emailAddress", userAddress.emailAddress)
            JsonParser.putJSONifNotNull(res, "address1", userAddress.address1)
            JsonParser.putJSONifNotNull(res, "address2", userAddress.address2)
            JsonParser.putJSONifNotNull(res, "address3", userAddress.address3)
            JsonParser.putJSONifNotNull(res, "address4", userAddress.address4)
            JsonParser.putJSONifNotNull(res, "address5", userAddress.address5)
            // A country subdivision (e.g. state or province)
            JsonParser.putJSONifNotNull(res, "administrativeArea", userAddress.administrativeArea)
            // City, town, neighborhood, or suburb.
            JsonParser.putJSONifNotNull(res, "locality", userAddress.locality)
            JsonParser.putJSONifNotNull(res, "sortingCode", userAddress.sortingCode)
        }
        return res
    }

    /**
     * Creates an instance of [PaymentsClient] for use in an [Activity] using the
     * environment and theme
     *
     * @param activity is the caller's activity.
     */
    fun createPaymentsClient(activity: Activity): PaymentsClient? {

        // check that Google Play is available
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(activity.baseContext)
        if (resultCode != ConnectionResult.SUCCESS) {
            val isUserResolvableError = googleApiAvailability.isUserResolvableError(resultCode)
            Log.i(
                TAG,
                "Google Play not available; resultCode=$resultCode, isUserResolvableError=$isUserResolvableError"
            )
            return null
        }
        val blueSnapService = BlueSnapService.instance
        val sdkRequest = blueSnapService.sdkRequest
        var googlePayMode = WalletConstants.ENVIRONMENT_PRODUCTION
        if (sdkRequest!!.isGooglePayTestMode) {
            googlePayMode = WalletConstants.ENVIRONMENT_TEST
        }
        // Create the client
        val walletOptions = WalletOptions.Builder()
            .setEnvironment(googlePayMode)
            .build()
        return Wallet.getPaymentsClient(activity, walletOptions)
    }

    /**
     * Builds [PaymentDataRequest] to be consumed by [PaymentsClient.loadPaymentData].
     */
    fun createPaymentDataRequest(googlePayClient: PaymentsClient): Task<PaymentData>? {
        val blueSnapService = BlueSnapService.instance
        val sdkRequest = blueSnapService.sdkRequest
        val merchantId = blueSnapService.getsDKConfiguration()!!.merchantId
        if (merchantId == null) {
            Log.e(TAG, "Missing merchantId from SDK init data")
            return null
        }
        val GATEWAY_TOKENIZATION_PARAMETERS =
            Arrays.asList(
                Pair.create(
                    "gatewayMerchantId",
                    merchantId.toString()
                )
            )
        val paramsBuilder =
            PaymentMethodTokenizationParameters.newBuilder()
                .setPaymentMethodTokenizationType(
                    WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY
                )
                .addParameter("gateway", GATEWAY_TOKENIZATION_NAME)
        for (param in GATEWAY_TOKENIZATION_PARAMETERS) {
            paramsBuilder.addParameter(param.first, param.second)
        }
        val request =
            createPaymentDataRequest(paramsBuilder.build(), sdkRequest)
        return googlePayClient.loadPaymentData(request)
    }

    private fun createPaymentDataRequest(
        params: PaymentMethodTokenizationParameters,
        sdkRequest: SdkRequestBase?
    ): PaymentDataRequest {
        val priceDetails = sdkRequest!!.priceDetails
        // AS-149: Google Pay price does not allow more than 2 digits after the decimal point
        val price =
            String.format("%.2f", if (priceDetails != null) priceDetails.amount else 0.0)
        val transactionInfo = createTransaction(
            price,
            if (priceDetails != null) priceDetails.currencyCode else "USD"
        )
        val merchantPaymentMethods: List<Int> = merchantPaymentMethods
        val shopperCheckoutRequirements =
            sdkRequest.shopperCheckoutRequirements
        return PaymentDataRequest.newBuilder()
            .setPhoneNumberRequired(shopperCheckoutRequirements.isShippingRequired)
            .setEmailRequired(shopperCheckoutRequirements.isEmailRequired)
            .setShippingAddressRequired(shopperCheckoutRequirements.isShippingRequired) // Omitting ShippingAddressRequirements all together means all countries are
            // supported.
            //.setShippingAddressRequirements(
            //        ShippingAddressRequirements.newBuilder()
            //                .addAllowedCountryCodes(Constants.SHIPPING_SUPPORTED_COUNTRIES)
            //                .build())
            .setTransactionInfo(transactionInfo)
            .addAllowedPaymentMethods(SUPPORTED_METHODS)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(merchantPaymentMethods)
                    .setAllowPrepaidCards(true) // todo: need to find out wehat this means
                    .setBillingAddressRequired(true) // Omitting this parameter will result in the API returning
                    // only a "minimal" billing address (post code only).
                    .setBillingAddressFormat(if (shopperCheckoutRequirements.isBillingRequired) WalletConstants.BILLING_ADDRESS_FORMAT_FULL else WalletConstants.BILLING_ADDRESS_FORMAT_MIN)
                    .build()
            )
            .setPaymentMethodTokenizationParameters(params) // If the UI is not required, a returning user will not be asked to select
            // a card. Instead, the card they previously used will be returned
            // automatically (if still available).
            // Prior whitelisting is required to use this feature.
            .setUiRequired(true)
            .build()
    }

    private val merchantPaymentMethods: List<Int>
        /**
         * Returns the allowed networks, based on the merchant info.
         * The allowed networks to be requested from Google-Pay API. If the user has cards from networks not
         * specified here in their account, these will not be offered for them to choose in the popup.
         *
         * @return
         */
        private get() {
            val creditCardBrands = BlueSnapService.instance.getsDKConfiguration()!!
                .supportedPaymentMethods.creditCardBrands
            val supportedNetworks: MutableList<Int> = ArrayList()
            for (ccBrand in creditCardBrands) {
                if (ccBrand.equals(CreditCardTypeResolver.VISA, ignoreCase = true)) {
                    supportedNetworks.add(WalletConstants.CARD_NETWORK_VISA)
                } else if (ccBrand.equals(CreditCardTypeResolver.AMEX, ignoreCase = true)) {
                    supportedNetworks.add(WalletConstants.CARD_NETWORK_AMEX)
                } else if (ccBrand.equals(CreditCardTypeResolver.DISCOVER, ignoreCase = true)) {
                    supportedNetworks.add(WalletConstants.CARD_NETWORK_DISCOVER)
                } else if (ccBrand.equals(CreditCardTypeResolver.JCB, ignoreCase = true)) {
                    supportedNetworks.add(WalletConstants.CARD_NETWORK_JCB)
                } else if (ccBrand.equals(CreditCardTypeResolver.MASTERCARD, ignoreCase = true)) {
                    supportedNetworks.add(WalletConstants.CARD_NETWORK_MASTERCARD)
                } else {
                    supportedNetworks.add(WalletConstants.CARD_NETWORK_OTHER)
                }
            }
            return supportedNetworks
        }

    /**
     * Determines if the user is eligible to Pay with Google by calling
     * [PaymentsClient.isReadyToPay]. The nature of this check depends on the methods set in
     * [.SUPPORTED_METHODS].
     *
     *
     * If [WalletConstants.PAYMENT_METHOD_CARD] is specified among supported methods, this
     * function will return true even if the user has no cards stored. Please refer to the
     * documentation for more information on how the check is performed.
     *
     * @param client used to send the request.
     */
    fun isReadyToPay(client: PaymentsClient): Task<Boolean> {
        val request =
            IsReadyToPayRequest.newBuilder()
        for (allowedMethod in SUPPORTED_METHODS) {
            request.addAllowedPaymentMethod(allowedMethod!!)
        }
        return client.isReadyToPay(request.build())
    }

    /**
     * Builds [TransactionInfo] for use with [.createPaymentDataRequest].
     *
     *
     * The price is not displayed to the user and must be in the following format: "12.34".
     *
     * @param price total of the transaction.
     */
    fun createTransaction(price: String?, currency: String?): TransactionInfo {
        return TransactionInfo.newBuilder()
            .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_ESTIMATED)
            .setTotalPrice(price!!)
            .setCurrencyCode(currency!!)
            .build()
    }

    companion object {
        val instance = GooglePayService()
    }
}