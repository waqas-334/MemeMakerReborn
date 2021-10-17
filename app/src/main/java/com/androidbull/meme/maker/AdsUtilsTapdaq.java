package com.androidbull.meme.maker;

import android.app.Activity;
import android.content.Context;
import android.widget.LinearLayout;

import com.tapdaq.sdk.TMBannerAdView;
import com.tapdaq.sdk.Tapdaq;
import com.tapdaq.sdk.common.TMBannerAdSizes;
import com.tapdaq.sdk.listeners.TMAdListener;

public class AdsUtilsTapdaq {


    public static class InterstitialListener extends TMAdListener {

        @Override
        public void didLoad() {
            // Ready to display the interstitial
        }

    }

    public static void LoadInterstitial(Activity activity) {
        Tapdaq.getInstance().loadVideo(activity, "default", new InterstitialListener());
        LoadStaticInterstitial(activity);
    }

    public static void ShowInterstitial(Activity activity) {
        if (Tapdaq.getInstance().isInterstitialReady(activity, "default")) {
            Tapdaq.getInstance().showInterstitial(activity, "default", new InterstitialListener());
        } else if (Tapdaq.getInstance().isVideoReady(activity, "default")) {
            Tapdaq.getInstance().showVideo(activity, "default", new InterstitialListener());
        }else {
            LoadInterstitial(activity);
        }

    }

    public static void LoadStaticInterstitial(Activity activity) {

        Tapdaq.getInstance().loadInterstitial(activity,  "default", new InterstitialListener());

    }

    public static void LoadAndShowBanner(Context context, LinearLayout bannerContainer)
    {
        TMBannerAdView ad = new TMBannerAdView(context); // Create ad view
        bannerContainer.addView(ad);
        ad.load((Activity) context, TMBannerAdSizes.STANDARD, new TMAdListener());
    }



}
