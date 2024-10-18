package com.bluesnap.androidapi.services

import android.text.TextUtils
import android.util.Patterns
import com.bluesnap.androidapi.Constants
import com.bluesnap.androidapi.models.BillingContactInfo
import com.bluesnap.androidapi.models.ContactInfo
import com.bluesnap.androidapi.models.CreditCard
import com.bluesnap.androidapi.models.CreditCardTypeResolver
import com.bluesnap.androidapi.models.ShippingContactInfo
import java.util.Arrays
import java.util.Calendar
import java.util.Locale

/**
 * Created by roy.biber on 25/02/2018.
 */
class BlueSnapValidator {
    private val calendarInstance: Calendar? = null
        /**
         * returns a Calendar Instance
         */
        private get() = if (field != null) field.clone() as Calendar else Calendar.getInstance()
    val STATE_NEEDED_COUNTRIES = arrayOf("US", "BR", "CA")

    /**
     * Credit Card Number Validation
     *
     * @param creditCard - a CreditCard Object
     * @see CreditCard
     */
    fun creditCardFullValidation(creditCard: CreditCard): Boolean {
        return (creditCardNumberValidation(creditCard.number ?: "")
                && creditCardCVVValidation(creditCard.cvc, creditCard.cardType)
                && creditCardExpiryDateValidation(
            creditCard.expirationYear?: 0,
            creditCard.expirationMonth?:0
        ))
    }

    /**
     * Credit Card Only Luhn Number Validation
     *
     * @param number - credit card number
     */
    private fun creditCardLuhnNumberValidation(number: String): Boolean {
        var isOdd = true
        var sum = 0
        for (index in number.length - 1 downTo 0) {
            val c = number[index]
            if (!Character.isDigit(c)) {
                return false
            }
            var digitInteger = ("" + c).toInt()
            isOdd = !isOdd
            if (isOdd) {
                digitInteger *= 2
            }
            if (digitInteger > 9) {
                digitInteger -= 9
            }
            sum += digitInteger
        }
        return sum % 10 == 0
    }

    /**
     * Credit Card Number Validation
     *
     * @param number - credit card number
     */
    fun creditCardNumberValidation(number: String): Boolean {
        if (TextUtils.isEmpty(number)) return false
        val rawNumber = number.trim { it <= ' ' }.replace("\\s+|-".toRegex(), "")
        return !(TextUtils.isEmpty(rawNumber) || !creditCardLuhnNumberValidation(rawNumber)) && number.length > 11 && number.length < 20
    }

    /**
     * Credit Card Expiry Date Validation
     *
     * @param expirationYear  - expiration date, Year
     * @param expirationMonth - expiration date, Month
     */
    fun creditCardExpiryDateValidation(expirationYear: Int, expirationMonth: Int): Boolean {
        return !(expirationMonth > 12 || expirationMonth < 1) && isDateInFuture(
            expirationMonth,
            expirationYear
        )
    }

    /**
     * Credit Card Expiry Date Validation
     *
     * @param expDateString - expiration date from TextView MM/YY
     */
    fun creditCardExpiryDateValidation(expDateString: String): Boolean {
        val mm: Int
        val yy: Int
        try {
            val mmyy = expDateString.split("\\/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            mm = Integer.valueOf(mmyy[0])
            yy = Integer.valueOf(mmyy[1])
            return creditCardExpiryDateValidation(yy, mm)
        } catch (e1: Exception) {
            try {
                val mmyy = expDateString.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                if (mmyy.size < 2 || TextUtils.isEmpty(mmyy[0]) || TextUtils.isEmpty(mmyy[1])) return creditCardExpiryDateValidation(
                    Integer.valueOf(
                        mmyy[1]
                    ), Integer.valueOf(mmyy[0])
                )
            } catch (e2: Exception) {
                return false
            }
        }
        return false
    }

    /**
     * Check if received date >= current month and current year
     *
     * @param year  - expiration date, Year
     * @param month - expiration date, Month
     */
    fun isDateInFuture(month: Int, year: Int): Boolean {
        var year = year
        val now = calendarInstance!!
        val currentYear = now[Calendar.YEAR]
        if (year < 2000) {
            year += 2000
        }
        return year > currentYear && year < 11 + currentYear || year == currentYear && month >= now[Calendar.MONTH] + 1
    }

    /**
     * Credit Card CVV Validation
     *
     * @param cvv      - credit card cvv number
     * @param cardType - card type associated to the cvv
     */
    fun creditCardCVVValidation(cvv: String?, cardType: String?): Boolean {
        if (TextUtils.isEmpty(cvv) || cvv == null) {
            return false
        }
        return if (cvv.length in 3..4) {
            if (null != cardType && CreditCardTypeResolver.AMEX == cardType) {
                cvv.length == 4
            } else cvv.length == 3
        } else false
    }

    /**
     * Check if Country has a State Field requirement (Required)
     *
     * @param countryText - ISO 3166-1 alpha-2 standard
     */
    fun checkCountryHasState(countryText: String?): Boolean {
        for (item in STATE_NEEDED_COUNTRIES) {
            if (item.equals(countryText, ignoreCase = true)) {
                return true
            }
        }
        return false
    }

    /**
     * Check if Country has a Zip Field requirement (Required)
     *
     * @param countryText - ISO 3166-1 alpha-2 standard
     * @return true if country has zip, false w.s.
     */
    fun checkCountryHasZip(countryText: String): Boolean {
        return !Arrays.asList(*Constants.COUNTRIES_WITHOUT_ZIP).contains(
            countryText.uppercase(
                Locale.getDefault()
            )
        )
    }

    /**
     * validate EditText by it's validation type
     *
     * @param editTextString - editText String
     * @param validationType - type of validation taken from EditTextFields Enum
     * @see EditTextFields
     */
    fun validateEditTextString(editTextString: String, validationType: EditTextFields): Boolean {
        val regex = "^[a-zA-Z0-9- ]*$"
        val editTextStringNoSpaces = editTextString.trim { it <= ' ' }
            .replace(" ".toRegex(), "")
        val splittedNames =
            editTextString.trim { it <= ' ' }.split(" ".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
        if (TextUtils.isEmpty(editTextStringNoSpaces) || editTextStringNoSpaces.length < 2 || TextUtils.isEmpty(
                validationType.toString()
            )
        ) return false
        return if (EditTextFields.NAME_FIELD == validationType && splittedNames.size < 2) {
            false
        } else if (EditTextFields.ZIP_FIELD == validationType && !editTextString.matches(regex.toRegex())) {
            false
        } else if (EditTextFields.EMAIL_FIELD == validationType && !Patterns.EMAIL_ADDRESS.matcher(
                editTextString
            ).matches()
        ) {
            false
        } else EditTextFields.STATE_FIELD != validationType || editTextString.length == 2
    }

    /**
     * Billing Info Validation
     *
     * @param billingContactInfo           [BillingContactInfo]
     * @param isEmailRequired       is Email Required
     * @param isFullBillingRequired is Full Billing Required
     */
    fun billingInfoValidation(
        billingContactInfo: BillingContactInfo,
        isEmailRequired: Boolean,
        isFullBillingRequired: Boolean
    ): Boolean {
        var validInput = contactInfoValidation(billingContactInfo, isFullBillingRequired)
        if (isEmailRequired) validInput = validInput and validateEditTextString(
            AndroidUtil.stringify(
                billingContactInfo.email
            ), EditTextFields.EMAIL_FIELD
        )
        return validInput
    }

    /**
     * Shipping Info Validation
     *
     * @param shippingContactInfo [ShippingContactInfo]
     */
    fun shippingInfoValidation(shippingContactInfo: ShippingContactInfo): Boolean {
        return contactInfoValidation(shippingContactInfo, true)
    }

    /**
     * Contact Info Validation
     *
     * @param contactInfo                       [ContactInfo]
     * @param isFullBillingRequiredOrIsShipping - boolean, if shipping or if full billing is required - true
     */
    private fun contactInfoValidation(
        contactInfo: ContactInfo,
        isFullBillingRequiredOrIsShipping: Boolean
    ): Boolean {
        var validInput = validateEditTextString(
            AndroidUtil.stringify(contactInfo.fullName),
            EditTextFields.NAME_FIELD
        )
        val country = AndroidUtil.stringify(contactInfo.country)
        if (checkCountryHasZip(country)) validInput = validInput and validateEditTextString(
            AndroidUtil.stringify(contactInfo.zip),
            EditTextFields.ZIP_FIELD
        )
        if (isFullBillingRequiredOrIsShipping) {
            if (checkCountryHasState(country)) validInput = validInput and validateEditTextString(
                AndroidUtil.stringify(contactInfo.state),
                EditTextFields.STATE_FIELD
            )
            validInput = validInput and validateEditTextString(
                AndroidUtil.stringify(contactInfo.city),
                EditTextFields.CITY_FIELD
            )
            validInput = validInput and validateEditTextString(
                AndroidUtil.stringify(contactInfo.address),
                EditTextFields.ADDRESS_FIELD
            )
        }
        return validInput
    }

    /**
     * validate Store Card switch - the shopper consent to store the credit card details, in case it is mandatory.
     * The shopper consent is mandatory only in the following cases: choose new card as payment method flow (shopper configuration), subscription mode.
     *
     * @param isShopperRequirements - is shopper configuration flow
     * @param isSubscriptionCharge  - is subscription mode
     * @param isStoreCard           - the switch value
     * @see EditTextFields
     */
    fun validateStoreCard(
        isShopperRequirements: Boolean,
        isSubscriptionCharge: Boolean,
        isStoreCard: Boolean
    ): Boolean {
        return if (isShopperRequirements || isSubscriptionCharge) isStoreCard else true
    }

    /**
     * EditText Field Names
     */
    enum class EditTextFields {
        NAME_FIELD, COUNTRY_FIELD, STATE_FIELD, CITY_FIELD, ADDRESS_FIELD, ZIP_FIELD, EMAIL_FIELD, PHONE_FIELD
    }
    companion object {
        val instance: BlueSnapValidator = BlueSnapValidator()

    }

}