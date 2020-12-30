package com.androidbull.meme.generator.ui.activities

import android.os.Bundle
import com.androidbull.meme.generator.helper.PremiumMemberObservable

abstract class AdsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PremiumMemberObservable.isPremiumUser.observe(this, { isPremium ->
            if (isPremium) {
                onPremiumMemberShipAcquired()   //TODO rename function
            } else {
                onPremiumMemberShipLost()       //TODO rename function
            }
        })
    }

    protected abstract fun onPremiumMemberShipLost()
    protected abstract fun onPremiumMemberShipAcquired()

}