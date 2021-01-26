package com.androidbull.meme.maker.ui.activities

import android.os.Bundle
import com.androidbull.meme.maker.helper.PremiumMemberObservable

abstract class AdsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        PremiumMemberObservable.isPremiumUser.observe(this, { isPremium ->
            if (isPremium) {
                onPremiumMemberShipAcquired()
            } else {
                onPremiumMemberShipLost()
            }
        })
    }

    protected abstract fun onPremiumMemberShipLost()
    protected abstract fun onPremiumMemberShipAcquired()

}