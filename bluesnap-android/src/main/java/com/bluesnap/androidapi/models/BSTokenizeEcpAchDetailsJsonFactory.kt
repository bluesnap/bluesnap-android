package com.bluesnap.androidapi.models
import org.json.JSONException
import org.json.JSONObject

object BSTokenizeEcpAchDetailsJsonFactory {
    /**
     * Creates a JSON object for tokenizing ECP/ACH details.
     *
     * @param ecpAchDetails The EcpAchDetails object containing ACH/ECP payment information.
     * @return A JSONObject ready to be sent to the BlueSnap API.
     * @throws JSONException In case of invalid JSON object construction.
     */
    @Throws(JSONException::class)
    fun createDataObject(ecpAchDetails: EcpAchDetails): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("paymentMethod", "ECP")
        jsonObject.put("ecpRoutingNumber", ecpAchDetails.routingNumber)
        jsonObject.put("ecpAccountType", ecpAchDetails.accountType.uppercase())
        jsonObject.put("ecpAccountNumber", ecpAchDetails.accountNumber)
        return jsonObject
    }
}
