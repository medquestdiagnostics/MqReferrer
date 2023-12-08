package com.medquestdiagnostics.referrer.util;

import android.webkit.JavascriptInterface;
import com.medquestdiagnostics.referrer.Home;

public class MyJavaScriptInterface {
    Home activity;

    public MyJavaScriptInterface(Home activity) {
        this.activity = activity;
    }

    @JavascriptInterface
    public void findCurrentUrl(String url) {
        //LogUtil.loge("findCurrentUrl: "+url);
        if(url == null)
            return;
        if(url.isEmpty())
            return;
        Home.currentUrl = url;
//        if(mWebviewPop != null){
//            LogUtil.loge("POPUP SHOWING");
//        }
    }
    @JavascriptInterface
    public void convertifyreset() {
        LogUtil.loge("convertifyreset");
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                activity.clearCache();
            }
        });
    }
}
