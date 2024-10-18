package com.bluesnap.androidapi.services

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.kount.api.DataCollector
import java.util.UUID

/**
 * Created by roy.biber on 14/11/2017.
 */
class KountService {
    private var kount: DataCollector? = null
    val kountSessionId: String?
        get() = Companion.kountSessionId

    fun setupKount(kountMerchantID: Int?, context: Context?, isProduction: Boolean) {
        kount = DataCollector.getInstance()
        kount?.setMerchantID(kountMerchantID!!)
        kount?.setContext(context)
        kount?.setLocationCollectorConfig(DataCollector.LocationConfig.COLLECT)
        if (isProduction) {
            kount?.setEnvironment(DataCollector.ENVIRONMENT_PRODUCTION)
            kount?.setDebug(false)
        } else {
            kount?.setEnvironment(DataCollector.ENVIRONMENT_TEST)
            kount?.setDebug(true)
        }

        //Run this inside it's on thread.
        Handler(Looper.getMainLooper())
            .post {
                Companion.kountSessionId = UUID.randomUUID().toString()
                Companion.kountSessionId =
                    Companion.kountSessionId?.replace("-", "") ?: ""
                kount?.collectForSession(
                    Companion.kountSessionId,
                    object : DataCollector.CompletionHandler {
                        /* Add handler code here if desired. The handler is optional. */
                        override fun completed(sessionID: String) {
                            Log.d(
                                TAG,
                                "Kount DataCollector completed"
                            )
                        }

                        override fun failed(sessionID: String, error: DataCollector.Error) {
                            Log.e(
                                TAG,
                                "Kount DataCollector failed: $error"
                            )
                        }
                    })
            }
    }

    companion object {
        private val TAG = KountService::class.java.simpleName
        val instance = KountService()
        const val KOUNT_MERCHANT_ID = 700000
        const val EXTRA_KOUNT_MERCHANT_ID = "com.bluesnap.intent.KOUNT_MERCHANT_ID"
        private const val KOUNT_REQUST_ID = 3
        private var kountSessionId: String? = null
    }
}