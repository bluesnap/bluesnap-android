package com.bluesnap.androidapi.models

import android.text.TextUtils
import android.util.Log
import com.bluesnap.androidapi.utils.JsonParser
import org.json.JSONObject

/**
 * Created by roy.biber on 07/11/2017.
 */
class CreditCard : BSModel {
    private val CARD_NUMBER = "cardNumber"

    @Transient
    var number: String? = null
        set(number) {
            val normalizedCardNumber = normalizeCardNumber(number)
            val cardType = CreditCardTypeResolver.getInstance().getType(normalizedCardNumber)
            if (normalizedCardNumber != null && normalizedCardNumber.length > 2) {
                this.cardType = cardType
                if (normalizedCardNumber.length > 4) cardLastFourDigits =    getNumberLastFourDigits(
                    normalizedCardNumber
                )
            }
            setIsNewCreditCard()
            field = normalizedCardNumber
        }
//    /**
//     * @return credit card number
//     */
//    fun getNumber(): String? {
//        return number
//    }
//
//    /**
//     * set credit card number (normalized) and updates last 4 digits and card type
//     *
//     * @param number - set credit card number String
//     */
//    fun setNumber(number: String?) {
//        val normalizedCardNumber = normalizeCardNumber(number)
//        val cardType = CreditCardTypeResolver.getInstance().getType(normalizedCardNumber)
//        if (normalizedCardNumber != null && normalizedCardNumber.length > 2) {
//            this.cardType = cardType
//            if (normalizedCardNumber.length > 4) setCardLastFourDigits(
//                getNumberLastFourDigits(
//                    normalizedCardNumber
//                )
//            )
//        }
//        setIsNewCreditCard()
//        this.number = normalizedCardNumber
//    }

    /**
     * @return cvv
     */
    /**
     * set cvv
     *
     * @param cvc - set cvc String
     */
    var cvc: String? = null
    private var tokenizedSuccess = false

    /**
     * Is a New Credit Card or a Previously Used (BlueSnap) one
     */
    var isNewCreditCard = false


    /**
     * set Credit Card as a new Credit Card
     */
    private fun setIsNewCreditCard() {
        if (!isNewCreditCard) isNewCreditCard = true
    }

    var cardLastFourDigits: String? = null
        get() {

            if (!TextUtils.isEmpty(field)) {
                return field
            }
            return if (number != null && number!!.length > 4) {
                number!!.substring(number!!.length - 4)
            } else null
        }
//    /**
//     * @return credit card Last Four Digits
//     */
//    fun getCardLastFourDigits(): String? {
//        if (!TextUtils.isEmpty(cardLastFourDigits)) {
//            return cardLastFourDigits
//        }
//        return if (number != null && number!!.length > 4) {
//            number!!.substring(number!!.length - 4)
//        } else null
//    }
//
//    /**
//     * set credit card Last Four Digits
//     *
//     * @param cardLastFourDigits - set credit card Last Four Digits String
//     */
//    private fun setCardLastFourDigits(cardLastFourDigits: String) {
//        this.cardLastFourDigits = cardLastFourDigits
//    }


    /**
     * @return cardType
     */
    /**
     * set cardType
     *
     * @param cardType - set cardType String
     * @see CreditCardTypeResolver
     */
    var cardType: String? = null
    var cardSubType: String? = null
    /**
     * @return expiration Date Month
     */
    /**
     * set expiration Date Month
     *
     * @param expirationMonth - expiration Date Month
     */
    var expirationMonth: Int? = null
    var expirationYear: Int? = null
        set(expirationYear) {
            var expirationYear = expirationYear
            if (expirationYear!! < 2000) {
                expirationYear += 2000
            }
            field = expirationYear
        }
//    /**
//     * @return expiration Date Year
//     */
//    fun getExpirationYear(): Int? {
//        return expirationYear
//    }
//
//    /**
//     * set expiration Date Year
//     * if year not 4 digits, change to 4 digits
//     *
//     * @param expirationYear - expiration Date Year
//     */
//    fun setExpirationYear(expirationYear: Int?) {
//        var expirationYear = expirationYear
//        if (expirationYear!! < 2000) {
//            expirationYear = expirationYear + 2000
//        }
//        this.expirationYear = expirationYear
//    }


    constructor()
    constructor(creditCard: CreditCard) {
        cardLastFourDigits = creditCard.cardLastFourDigits
        cardType = creditCard.cardType
        cardSubType = creditCard.cardSubType
        expirationMonth = creditCard.expirationMonth
        expirationYear = creditCard.expirationYear
    }

    /**
     * update credit card details
     *
     * @param creditCard - credit card object
     */
    fun update(creditCard: CreditCard) {
        expirationMonth = creditCard.expirationMonth
        expirationYear = creditCard.expirationYear
//        setExpirationYear(creditCard.getExpirationYear())
        cvc = creditCard.cvc
        tokenizedSuccess = false
        number = creditCard.number
    }

    /**
     * update credit card details
     *
     * @param creditCardNumberString - credit card number String
     * @param expDateString          - credit card expiration date String
     * @param cvvString              - credit card cvv String
     */
    fun update(creditCardNumberString: String?, expDateString: String, cvvString: String?) {
        setExpDateFromString(expDateString)
        cvc = cvvString
        tokenizedSuccess = false
        number = creditCardNumberString
    }

    /**
     * trim number and deletes spaces
     */
    private fun normalizeCardNumber(number: String?): String? {
        return number?.trim { it <= ' ' }?.replace("\\s+|-".toRegex(), "")
    }


    /**
     * get last four digits from number
     *
     * @param normalizedCardNumber - normalized Card Number (see normalizeCardNumber function)
     * @return normalized credit card Last Four Digits
     */
    private fun getNumberLastFourDigits(normalizedCardNumber: String): String {
        return normalizedCardNumber.substring(normalizedCardNumber.length - 4)
    }


    /**
     * change Expiration Month Integer To Two Digits (MM) String
     *
     * @return Month String representation
     */
    private fun changeExpirationMonthIntegerToTwoDigitsString(): String {
        return if (expirationMonth!! < 10) "0$expirationMonth" else expirationMonth.toString()
    }

    val expirationDate: String
        /**
         * get Expiration Date
         *
         * @return MM/YYYY
         */
        get() {
            if (expirationYear!! < 2000) {
                expirationYear = expirationYear!! + 2000
            }
            return changeExpirationMonthIntegerToTwoDigitsString() + "/" + expirationYear
        }
    val expirationDateForEditTextAndSpinner: String?
        /**
         * get Expiration Date For EditText And TextView
         *
         * @return MM/YY
         */
        get() = if (null != expirationMonth && null != expirationYear && !(expirationMonth == 0 || expirationYear == 0)) (changeExpirationMonthIntegerToTwoDigitsString()
                + "/"
                + if (expirationYear!! > 2000) expirationYear!! - 2000 else expirationYear) else null

    /**
     * set Expiration Date From String
     *
     * @param expDateString - expiration Date From EditText And TextView
     */
    fun setExpDateFromString(expDateString: String) {
        expirationMonth = 0
        expirationYear = 0
        try {
            if ("" != expDateString) {
                val mmyy = expDateString.split("\\/".toRegex()).dropLastWhile { it.isEmpty() }
                    .toTypedArray()
                expirationMonth = Integer.valueOf(mmyy[0])
//                setExpirationYear(Integer.valueOf(mmyy[1]))
                expirationYear = Integer.valueOf(mmyy[1])

                return
            }
        } catch (e: Exception) {
            Log.e("setEX", "setexp", e)
        }
        try {
            val mmyy = expDateString.split("/".toRegex()).dropLastWhile { it.isEmpty() }
                .toTypedArray()
            if (mmyy.size < 2 || TextUtils.isEmpty(mmyy[0]) || TextUtils.isEmpty(mmyy[1])) return
            expirationMonth = Integer.valueOf(mmyy[0])
//            setExpirationYear(Integer.valueOf(mmyy[1]))
            expirationYear = Integer.valueOf(mmyy[1])
            return
        } catch (e: Exception) {
            Log.e("setEX", "setexp", e)
        }
    }

    /**
     * set Tokeanization Successful
     */
    fun setTokenizationSuccess() {
        tokenizedSuccess = true
    }


    /**
     * create JSON object from Credit Card
     *
     * @return JSONObject
     */
    override fun toJson(): JSONObject {
        val jsonObject = JSONObject()
        JsonParser.putJSONifNotNull(jsonObject, SECURITY_CODE, cvc)
        JsonParser.putJSONifNotNull(jsonObject, CARD_NUMBER, number)
        JsonParser.putJSONifNotNull(jsonObject, CARD_LAST_FOUR_DIGITS, cardLastFourDigits)
        JsonParser.putJSONifNotNull(
            jsonObject, EXPIRATION_MONTH,
            expirationMonth
        )
        JsonParser.putJSONifNotNull(jsonObject, EXPIRATION_YEAR, expirationYear)
        JsonParser.putJSONifNotNull(jsonObject, CARD_TYPE, cardType)
        JsonParser.putJSONifNotNull(
            jsonObject, CARD_SUB_TYPE,
            cardSubType
        )
        return jsonObject
    }

    override fun toString(): String {
        return "CreditCard{}"
    }

    companion object {
        private const val SECURITY_CODE = "securityCode"
        private const val CARD_LAST_FOUR_DIGITS = "cardLastFourDigits"
        private const val EXPIRATION_MONTH = "expirationMonth"
        private const val EXPIRATION_YEAR = "expirationYear"
        private const val CARD_TYPE = "cardType"
        const val CARD_SUB_TYPE = "cardSubType"
        fun fromJson(jsonObject: JSONObject?): CreditCard? {
            if (jsonObject == null) {
                return null
            }
            val creditCard = CreditCard()
            creditCard.cardLastFourDigits =   JsonParser.getOptionalString(
                jsonObject,
                CARD_LAST_FOUR_DIGITS
            )
            creditCard.cardType =
                JsonParser.getOptionalString(jsonObject, CARD_TYPE)
            creditCard.cardSubType =
                JsonParser.getOptionalString(jsonObject, CARD_SUB_TYPE)
            creditCard.expirationMonth = Integer.valueOf(
                JsonParser.getOptionalString(
                    jsonObject,
                    EXPIRATION_MONTH
                )
            )
            creditCard.expirationYear =   Integer.valueOf(
                JsonParser.getOptionalString(
                    jsonObject,
                    EXPIRATION_YEAR
                )
            )
            return creditCard
        }
    }
}