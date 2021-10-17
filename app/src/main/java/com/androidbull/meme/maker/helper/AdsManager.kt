package com.androidbull.meme.maker.helper

import android.util.Log
import android.view.ViewGroup
//import com.facebook.ads.*

object AdsManager {

//    private const val TAG = "AdsManager"
//    private var bannerAds: MutableList<AdView> = mutableListOf()

//    fun loadAndShowBannerAd(
//        adId: String,
//        adContainer: ViewGroup,
//        adListenerParam: AdListener? = null
//    ) {

//        var bannerAd: AdView? = null
//
//        // check "if" already loaded // To counter repeating call for same AD
//        bannerAds.forEach { adView ->
//            if (adView.placementId == adId) {
//                bannerAd = adView
//                return
//            }
//        }
//
//        // "else"
//        if (bannerAd == null)
//            bannerAd = AdView(
//                AppContext.getInstance().context,
//                adId,
//                AdSize.BANNER_HEIGHT_50
//            )
//
//
//        val adListener = object : AdListener {
//            override fun onAdLoaded(ad: Ad?) {
//                Log.d(TAG, "onAdLoaded: Banner ad one loaded")
//                showBannerAd(bannerAd!!, adContainer)
//            }
//
//            override fun onAdClicked(ad: Ad?) {
//                Log.d(TAG, "onAdClicked: Banner ad one clicked")
//            }
//
//            override fun onError(ad: Ad?, error: AdError?) {
//                Log.d(TAG, "onError: Banner ad one error : ${error?.errorMessage}")
//            }
//
//            override fun onLoggingImpression(ad: Ad?) {
//
//            }
//        }
//
//        bannerAd?.let {
//            if (adListenerParam != null) {
//                it.loadAd(it.buildLoadAdConfig().withAdListener(adListenerParam).build())
//            } else {
//                it.loadAd(it.buildLoadAdConfig().withAdListener(adListener).build())
//            }
//            bannerAds.add(it)
//        }
//    }


//    private fun showBannerAd(bannerAd: AdView, adContainer: ViewGroup) {
//        val parent = bannerAd.parent as ViewGroup?
//        parent?.let {
//            it.removeView(bannerAd)
//            it.invalidate()
//        }
//        adContainer.removeAllViews()
//        adContainer.addView(bannerAd)
//    }

//    fun removeAd(adId: String) {
//        val bannerAdsIterator = bannerAds.iterator()
//        while (bannerAdsIterator.hasNext()) {
//            val bannerAd = bannerAdsIterator.next()
//            if (bannerAd.placementId == adId) {
//                val parent = bannerAd.parent as ViewGroup?
//                parent?.let {
//                    it.removeView(bannerAd)
//                    it.removeAllViews()
//                    it.invalidate()
//                }
//                bannerAd.destroy()
//                bannerAdsIterator.remove()
//                break
//            }
//        }
//    }

//    fun removeAds() {
//        bannerAds.forEach { adView ->
//            val parent = adView.parent as ViewGroup?
//            parent?.let {
//                it.removeView(adView)
//                it.removeAllViews()
//                it.invalidate()
//            }
//            adView.destroy()
//        }
//    }
}
