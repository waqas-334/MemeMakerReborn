package com.androidbull.meme.generator.helper

import androidx.lifecycle.MutableLiveData

object PremiumMemberObservable {
    var isPremiumUser = MutableLiveData(
        PreferenceManager.getInstance().getBoolean(     // initial value
            PREF_IS_PREMIUM_USER
        )
    )
}