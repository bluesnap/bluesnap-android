package com.bluesnap.androidapi.models

import android.util.Log
import android.view.View
import com.bluesnap.androidapi.R
import com.bluesnap.androidapi.services.BSPaymentRequestException
import com.bluesnap.androidapi.views.activities.BluesnapCheckoutActivity

/**
 * A Request for payment process in the SDK.
 * A new SdkRequest should be used for each purchase.
 */
class SdkRequest : SdkRequestBase {
    constructor()
    constructor(amount: Double?, currencyNameCode: String?) {
        shopperCheckoutRequirements = ShopperCheckoutRequirements()
        priceDetails = PriceDetails(amount, currencyNameCode, 0.0)
    }

    constructor(
        amount: Double?,
        currencyNameCode: String?,
        shopperCheckoutRequirements: ShopperCheckoutRequirements?
    ) {
        this.shopperCheckoutRequirements = ShopperCheckoutRequirements(shopperCheckoutRequirements)
        priceDetails = PriceDetails(amount, currencyNameCode, 0.0)
    }

    constructor(
        amount: Double?,
        currencyNameCode: String?,
        billingRequired: Boolean,
        emailRequired: Boolean,
        shippingRequired: Boolean
    ) {
        shopperCheckoutRequirements =
            ShopperCheckoutRequirements(shippingRequired, billingRequired, emailRequired)
        priceDetails = PriceDetails(amount, currencyNameCode, 0.0)
    }

    override fun setActivate3DS(activate3DS: Boolean) {
        this.activate3DS = activate3DS
    }

    override fun isAllowCurrencyChange(): Boolean {
        return allowCurrencyChange
    }

    override fun isHideStoreCardSwitch(): Boolean {
        return hideStoreCardSwitch
    }

    @kotlin.Throws(BSPaymentRequestException::class)
    override fun verify(): Boolean {
        priceDetails.verify()
        return true
    }

    /**
     * set Sdk Result with sdk Reuqst details
     *
     * @param sdkResult - [SdkResult]
     */
    override fun setSdkResult(sdkResult: SdkResult) {
        // Copy values from request
        sdkResult.result = BluesnapCheckoutActivity.BS_CHECKOUT_RESULT_OK
        sdkResult.amount = priceDetails.amount
        sdkResult.currencyNameCode = priceDetails.currencyCode
    }

    override fun updateTax(shippingCountry: String?, shippingState: String?) {
        val taxCalculator = taxCalculator
        if (getShopperCheckoutRequirements().isShippingRequired && taxCalculator != null) {
            val priceDetails = getPriceDetails()
            Log.d(
                TAG,
                "Calling taxCalculator; shippingCountry=$shippingCountry, shippingState=$shippingState, priceDetails=$priceDetails"
            )
            taxCalculator.updateTax(shippingCountry, shippingState, priceDetails)
            Log.d(
                TAG,
                "After calling taxCalculator; priceDetails=$priceDetails"
            )
        }
    }

    override fun getBuyNowButtonText(view: View): String {
        return getStringFormatAmount(
            view.resources.getString(R.string.pay),
            priceDetails.currencyCode,
            priceDetails.amount
        )
    }

    companion object {
        private val TAG: String = SdkRequest::class.java.getSimpleName()
    }
}