package com.bluesnap.androidapi.models

class EcpAchDetails(
    val paymentMethod: String = "ECP",
    val routingNumber: String,
    val accountNumber: String,
    val accountType: String)
{

    val valid: Boolean

    init {
        validateAccountType(accountType)
        validatePaymentMethod(paymentMethod)
        valid = true
    }

    private fun validateAccountType(accountType: String) {
        val validAccountTypes = setOf(
            "CONSUMER_CHECKING",
            "CONSUMERSAVINGS",
            "CORPORATECHECKING",
            "CORPORATE_SAVINGS"
        )
        if (accountType !in validAccountTypes) {
            throw IllegalArgumentException("Invalid account type: $accountType")
        }
    }

    private fun validatePaymentMethod(paymentMethod: String) {
        val validPaymentMethods = setOf("ECP", "ACH")
        if (paymentMethod !in validPaymentMethods) {
            throw IllegalArgumentException("Invalid payment method: $paymentMethod")
        }
    }
}

