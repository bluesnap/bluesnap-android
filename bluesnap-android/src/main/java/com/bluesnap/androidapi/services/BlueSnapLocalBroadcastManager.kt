package com.bluesnap.androidapi.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

/**
 * Created by roy.biber on 05/03/2018.
 */
object BlueSnapLocalBroadcastManager {
    const val SUMMARIZED_BILLING_EDIT = "com.bluesnap.intent.SUMMARIZED_BILLING_EDIT"
    const val SUMMARIZED_BILLING_CHANGE = "com.bluesnap.intent.SUMMARIZED_BILLING_CHANGE"
    const val SUMMARIZED_SHIPPING_EDIT = "com.bluesnap.intent.SUMMARIZED_SHIPPING_EDIT"
    const val SUMMARIZED_SHIPPING_CHANGE = "com.bluesnap.intent.SUMMARIZED_SHIPPING_CHANGE"
    const val SHIPPING_SWITCH_ACTIVATED = "com.bluesnap.intent.SHIPPING_SWITCH_ACTIVATED"
    const val COUNTRY_CHANGE_REQUEST = "com.bluesnap.intent.COUNTRY_CHANGE_REQUEST"
    const val COUNTRY_CHANGE_RESPONSE = "com.bluesnap.intent.COUNTRY_CHANGE_RESPONSE"
    const val STATE_CHANGE_REQUEST = "com.bluesnap.intent.STATE_CHANGE_REQUEST"
    const val STATE_CHANGE_RESPONSE = "com.bluesnap.intent.STATE_CHANGE_RESPONSE"
    const val CURRENCY_UPDATED_EVENT = "com.bluesnap.intent.CURRENCY_UPDATED_EVENT"
    const val ONE_LINE_CC_EDIT_FINISH = "com.bluesnap.intent.ONE_LINE_CC_EDIT_FINISH"
    const val NEW_CARD_SHIPPING_CHANGE = "com.bluesnap.intent.NEW_CARD_SHIPPING_CHANGE"

    /**
     * a LocalBroadcastManager sendMessage with an extra message inside the intent
     * the message title is the event
     *
     * @param context - [Context]
     * @param event   - event String
     * @param Msg     - message for intent extra
     * @param tag     -  simple name string of class
     */
    fun sendMessage(context: Context?, event: String?, Msg: Boolean, tag: String?) {
        Log.d(tag, event!!)
        val intent = Intent(event)
        intent.putExtra(event, Msg)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    /**
     * a LocalBroadcastManager sendMessage with an extra message inside the intent
     * the message title is the event
     *
     * @param context - [Context]
     * @param event   - event String
     * @param Msg     - message for intent extra
     * @param tag     -  simple name string of class
     */
    fun sendMessage(context: Context?, event: String?, Msg: String?, tag: String?) {
        Log.d(tag, event!!)
        val intent = Intent(event)
        intent.putExtra(event, Msg)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    /**
     * a LocalBroadcastManager sendMessage with two extra messages inside the intent
     *
     * @param context - [Context]
     * @param event   - event String
     * @param title1  - first title for intent extra
     * @param msg1    - first message for intent extra
     * @param title2  - second title for intent extra
     * @param msg2    - second message for intent extra
     * @param tag     -  simple name string of class
     */
    fun sendMessage(
        context: Context?,
        event: String?,
        title1: String?,
        msg1: String?,
        title2: String?,
        msg2: String?,
        tag: String?
    ) {
        Log.d(tag, event!!)
        val intent = Intent(event)
        intent.putExtra(title1, msg1)
        intent.putExtra(title2, msg2)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(intent)
    }

    /**
     * a LocalBroadcastManager sendMessage
     *
     * @param context - [Context]
     * @param event   - event String
     * @param tag     -  simple name string of class
     */
    fun sendMessage(context: Context?, event: String?, tag: String?) {
        Log.d(tag, event!!)
        LocalBroadcastManager.getInstance(context!!).sendBroadcast(Intent(event))
    }

    /**
     * a LocalBroadcastManager registerReceiver
     *
     * @param context           - [Context]
     * @param event             - event String
     * @param broadcastReceiver - [BroadcastReceiver]
     */
    fun registerReceiver(context: Context?, event: String?, broadcastReceiver: BroadcastReceiver?) {
        LocalBroadcastManager.getInstance(context!!).registerReceiver(
            broadcastReceiver!!,
            IntentFilter(event)
        )
    }

    /**
     * a LocalBroadcastManager unregisterReceiver
     *
     * @param context           - [Context]
     * @param broadcastReceiver - [BroadcastReceiver]
     */
    fun unregisterReceiver(context: Context?, broadcastReceiver: BroadcastReceiver?) {
        LocalBroadcastManager.getInstance(context!!).unregisterReceiver(broadcastReceiver!!)
    }
}