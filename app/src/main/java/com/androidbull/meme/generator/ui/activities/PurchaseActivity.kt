package com.androidbull.meme.generator.ui.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingClient.BillingResponseCode.*
import com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
import com.androidbull.meme.generator.R
import com.androidbull.meme.generator.helper.*
import com.androidbull.meme.generator.ui.dialogs.ErrorDialog
import com.google.android.material.appbar.MaterialToolbar

private const val TAG = "PurchaseActivity"

class PurchaseActivity : BaseActivity(), PurchasesUpdatedListener {

    private var billingClient: BillingClient? = null
    private lateinit var tbPurchases: MaterialToolbar
    private lateinit var btnPurchase: Button
    private val skuDetailsList: MutableList<SkuDetails> = mutableListOf()
    private lateinit var progressDialog: ProgressDialog
    private val productId = PRODUCT_REMOVE_ADS_ID

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_purchase)

        initUi()
        initToolbar()

        progressDialog = ProgressDialog(this)
        progressDialog.setCancelable(false)
        progressDialog.setCanceledOnTouchOutside(false)
        progressDialog.setTitle(getString(R.string.str_please_wait_))

        btnPurchase.setOnClickListener {
            progressDialog.show()
            instantiateAndConnectToPlayBillingService()
        }
    }

    private fun initUi() {
        tbPurchases = findViewById(R.id.tbPurchases)
        btnPurchase = findViewById(R.id.btnPurchase)
    }

    override fun onResume() {
        queryPurchases()
        super.onResume()
    }


    private fun showErrorDialog(errorMessage: String) {
        try {
            progressDialog.hide()
            ErrorDialog.newInstance(errorMessage)
                .show(supportFragmentManager, FRAGMENT_ERROR_DIALOG_TAG)
        } catch (ex: Exception) {
            ex.printStackTrace()
        }

    }

    private fun queryPurchases() {
        billingClient?.let { billingClient ->
            if (billingClient.isReady) {
                val purchasesResult = billingClient.queryPurchases(BillingClient.SkuType.INAPP)
                when (purchasesResult.responseCode) {
                    OK -> {
                        purchasesResult.purchasesList?.let { purchases ->
                            for (purchase in purchases) {
                                acknowledgeNonConsumableProduct(purchase)
                            }
                        }
                    }
                    USER_CANCELED -> {
                        Log.i(TAG, "User cancelled purchase flow.")
                    }
                    SERVICE_DISCONNECTED -> {
                        Log.i(TAG, "Service disconnected.")
                    }
                    else -> {
                        Toast.makeText(
                            this,
                            "onPurchaseUpdated error: ${purchasesResult.responseCode}",
                            Toast.LENGTH_SHORT
                        ).show()

                        Log.i(TAG, "onPurchaseUpdated error: ${purchasesResult.responseCode}")
                    }
                }
            }
        }
    }


    private fun initToolbar() {
        setSupportActionBar(tbPurchases)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    private fun instantiateAndConnectToPlayBillingService() {
        billingClient = BillingClient
            .newBuilder(applicationContext)
            .enablePendingPurchases() // required or app will crash
            .setListener(this)
            .build()

        connectToPlayBillingService()

    }


    private fun connectToPlayBillingService() {
        billingClient?.let { billingClient ->
            if (!billingClient.isReady) {
                billingClient.startConnection(object : BillingClientStateListener {
                    override fun onBillingSetupFinished(billingResult: BillingResult) {
                        if (billingResult.responseCode == OK) {
                            Log.d(TAG, "Billing client successfully set up")
                            queryOneTimeProducts()
                        } else {
                            val errorMessage =
                                getString(R.string.something_went_wrong) + " " + billingResult.debugMessage + getString(
                                    R.string.str_check_playstore_login
                                )
                            showErrorDialog(errorMessage)
                            Log.d(TAG, billingResult.debugMessage)
                        }
                    }

                    override fun onBillingServiceDisconnected() {
                        val errorMessage =
                            getString(R.string.str_billing_service_disconnected)
                        showErrorDialog(errorMessage)
                        Log.d(TAG, "Billing service disconnected")
                    }
                })
            } else {
                queryOneTimeProducts()
            }
        }
    }

    private fun queryOneTimeProducts() {
        val skuListToQuery = ArrayList<String>()
        skuListToQuery.add(productId)   // TODO change test id

        val params = SkuDetailsParams.newBuilder()
        params.setSkusList(skuListToQuery)
            .setType(BillingClient.SkuType.INAPP)

        billingClient?.let { billingClient ->
            billingClient.querySkuDetailsAsync(
                params.build()
            ) { result, skuDetails ->
                if (result.responseCode == OK) {
                    if (skuDetails != null && skuDetails.isNotEmpty()) {
                        onSkuDetailsFetched(skuDetails)
                    } else {
                        val errorMessage =
                            getString(R.string.str_no_products_found)
                        showErrorDialog(errorMessage)
                        Log.d(TAG, getString(R.string.str_no_products_found))
                    }
                } else {
                    val errorMessage =
                        getString(R.string.something_went_wrong) + " " + result.debugMessage
                    showErrorDialog(errorMessage)
                    Log.d(TAG, result.debugMessage)
                }
            }
        }
    }

    private fun onSkuDetailsFetched(skuDetailsListParam: List<SkuDetails>) {
        updateSkuDetails(skuDetailsListParam)
        skuDetailsListParam.forEach { skuDetail ->
            if (skuDetail.sku == productId) {
                launchPurchaseFlow(skuDetail)
                return
            }
        }

        val errorMessage =
            getString(R.string.str_remove_ads_product_not_found)
        showErrorDialog(errorMessage)

    }

    private fun updateSkuDetails(skuDetailsListParam: List<SkuDetails>) {
        skuDetailsList.clear()
        skuDetailsListParam.forEach {
            skuDetailsList.add(it)
        }
    }

    private fun launchPurchaseFlow(skuDetails: SkuDetails) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSkuDetails(skuDetails)
            .build()
        billingClient?.let { billingClient ->
            val result = billingClient.launchBillingFlow(this, flowParams)
            if (result.responseCode == OK) {
                progressDialog.dismiss()
            } else {
                val errorMessage =
                    getString(R.string.something_went_wrong)
                showErrorDialog(errorMessage)
            }
        }
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
        if (result.responseCode == OK && purchases != null) {
            for (purchase in purchases) {
                acknowledgeNonConsumableProduct(purchase)
            }
        } else {
            val errorMessage =
                getString(R.string.str_purchase_unsuccessful) + " " + result.debugMessage
//            showErrorDialog(errorMessage)
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
            Log.i(TAG, "onPurchaseUpdated error: ${result.responseCode}")
        }
    }

    private fun acknowledgeNonConsumableProduct(purchase: Purchase) {
        if (purchase.purchaseState == PURCHASED && isSignatureValid(purchase)) {
            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                billingClient?.let { billingClient ->
                    billingClient.acknowledgePurchase(
                        acknowledgePurchaseParams.build()
                    ) { result ->
                        if (result.responseCode == OK) {
                            Toast.makeText(
                                this,
                                getString(R.string.str_purchase_successful),
                                Toast.LENGTH_SHORT
                            ).show()
                            if (purchase.sku == PRODUCT_REMOVE_ADS_ID) {
                                upgradeToPremium()
                            }
                        } else {
                            Toast.makeText(
                                this,
                                getString(R.string.str_purchase_acknoweledgement_failed) + " " + result.debugMessage,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
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

    override fun onDestroy() {
        if (progressDialog != null)
            progressDialog.dismiss()
        super.onDestroy()
    }

}
