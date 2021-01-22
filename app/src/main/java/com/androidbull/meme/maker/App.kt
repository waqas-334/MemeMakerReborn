package com.androidbull.meme.maker

import android.app.Application
import android.util.Log
import android.widget.Toast
import com.android.billingclient.api.*
import com.androidbull.meme.maker.data.db.room.AppDatabase
import com.androidbull.meme.maker.helper.*
import com.facebook.ads.AudienceNetworkAds

const val TAG = "MemeMakerApp"

class App : Application() {

    private lateinit var billingClient: BillingClient

    override fun onCreate() {
        super.onCreate()

        AppDatabase.getInstance(this)
        AppContext.getInstance().initialize(this)
        AudienceNetworkAds.buildInitSettings(this)
            .withInitListener {
                Log.d(TAG, it.message)
            }
            .initialize()

        instantiateAndConnectToPlayBillingService()

    }

    private fun instantiateAndConnectToPlayBillingService() {
        billingClient = BillingClient
            .newBuilder(applicationContext)
            .enablePendingPurchases() // required or app will crash
            .setListener { _, _ ->
            }
            .build()

        connectToPlayBillingService()
    }

    private fun connectToPlayBillingService(): Boolean {
        if (!billingClient.isReady) {
            billingClient.startConnection(object : BillingClientStateListener {

                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        Log.d(TAG, "Billing client successfully set up")
                        queryPurchases()
                    } else {
                        Log.d(TAG, billingResult.debugMessage)
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Log.d(TAG, "Billing service disconnected")
                }
            })

            return true
        }
        return false
    }

    private fun queryPurchases() {
        if (billingClient.isReady) {
            val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
            when (purchasesResult.responseCode) {
                BillingClient.BillingResponseCode.OK -> {
                    purchasesResult.purchasesList?.let { purchases ->
                        for (purchase in purchases) {
                            if (purchase.sku == PRODUCT_REMOVE_ADS_ID) {
                                acknowledgeAndUpgradeToPremium(purchase)
                            }
                        }
                    }
                }
                BillingClient.BillingResponseCode.USER_CANCELED -> {
                    Log.i(TAG, "User cancelled purchase flow.")
                }
                BillingClient.BillingResponseCode.SERVICE_DISCONNECTED -> {
                    Log.i(TAG, "Service disconnected.")
                }
                else -> {
                    Log.i(TAG, "onPurchaseUpdated error: ${purchasesResult.responseCode}")
                }
            }
        }
    }

    private fun acknowledgeAndUpgradeToPremium(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED && isSignatureValid(purchase)) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                billingClient.acknowledgePurchase(
                    acknowledgePurchaseParams.build()
                ) { result ->
                    if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                        if (purchase.sku == PRODUCT_REMOVE_ADS_ID) {
                            upgradeToPremium()
                        }
                    } else {
                        Log.d(TAG, result.debugMessage)
                        Toast.makeText(this, result.debugMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                upgradeToPremium()
            }
        }
    }

    private fun isSignatureValid(purchase: Purchase): Boolean {
        return Security.verifyPurchase(
            purchase.sku,
            getDecodedBase64PublicKey(),
            purchase.originalJson,
            purchase.signature
        )
    }
}
