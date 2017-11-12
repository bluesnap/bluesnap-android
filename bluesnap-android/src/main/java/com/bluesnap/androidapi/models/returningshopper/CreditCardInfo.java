package com.bluesnap.androidapi.models.returningshopper;

import android.support.annotation.Nullable;

import com.bluesnap.androidapi.services.AndroidUtil;

import org.json.JSONObject;

/**
 * Created by roy.biber on 07/11/2017.
 */

public class CreditCardInfo {
    private static final String TAG = CreditCardInfo.class.getSimpleName();
    private static final String BILLINGCONTACTINFO = "billingContactInfo";
    private static final String CREDITCARD = "creditCard";

    private ContactInfo billingContactInfo;
    private CreditCard creditCard;

    public CreditCardInfo(@Nullable JSONObject creditCardInfo) {
        billingContactInfo = new ContactInfo((JSONObject) AndroidUtil.getObjectFromJsonObject(creditCardInfo, BILLINGCONTACTINFO, TAG));
        creditCard = new CreditCard((JSONObject) AndroidUtil.getObjectFromJsonObject(creditCardInfo, CREDITCARD, TAG));
    }

    public CreditCardInfo(ContactInfo billingContactInfo, CreditCard creditCard) {
        this.billingContactInfo = billingContactInfo;
        this.creditCard = creditCard;
    }

    public CreditCardInfo() {
        billingContactInfo = new ContactInfo();
        creditCard = new CreditCard();
    }

    public ContactInfo getBillingContactInfo() {
        return billingContactInfo;
    }

    public void setBillingContactInfo(ContactInfo billingContactInfo) {
        this.billingContactInfo = billingContactInfo;
    }

    public CreditCard getCreditCard() {
        return creditCard;
    }

    public void setCreditCard(CreditCard creditCard) {
        this.creditCard = creditCard;
    }
}
