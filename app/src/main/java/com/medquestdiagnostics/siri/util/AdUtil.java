package com.medquestdiagnostics.siri.util;

import android.view.ViewGroup;
import android.widget.RelativeLayout;
import com.medquestdiagnostics.siri.Home;
import com.medquestdiagnostics.siri.R;
import com.medquestdiagnostics.siri.Settings;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;


public class AdUtil {

    public static void loadBannerAd(Home activity, ViewGroup adParent) {
        if(Settings.BANNER_AD){
            adParent.removeAllViews();
            AdRequest adRequest = new AdRequest.Builder().build();
            AdView adView = new AdView(activity);
            adView.setAdSize(AdSize.SMART_BANNER);
            adView.setAdUnitId(activity.getResources().getString(R.string.admobBannerId));
            adView.setBackgroundColor(activity.getResources().getColor(android.R.color.transparent));
           // adView.loadAd(adRequest);
            RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            params.addRule(RelativeLayout.ALIGN_BOTTOM, RelativeLayout.TRUE);
            adParent.addView(adView, params);
        }
    }

   /* public static void loadInterstitialAd(Home activity){
        LogUtil.loge("loadInterstitialAd");
        Home.linkClicks = 0;
        if(Settings.INTERSTITIAL_AD){
            final InterstitialAd mInterstitialAd = new InterstitialAd(activity);
            mInterstitialAd.setAdUnitId(activity.getResources().getString(R.string.admobInterstitialId));
            mInterstitialAd.loadAd(new AdRequest.Builder().build());
            mInterstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    LogUtil.loge("onAdLoaded");
                    super.onAdLoaded();
                    mInterstitialAd.show();
                }
            });
        }
    }*/
}
