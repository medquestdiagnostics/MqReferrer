package com.medquestdiagnostics.referrer;

import android.app.Application;

public class MyApp extends Application{

   /* @Override
    public void onCreate() {
        super.onCreate();
        initialize(this);
        OneSignal.initWithContext(this)
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .setNotificationOpenedHandler(new OneSignal.NotificationOpenedHandler() {
                    @Override
                    public void notificationOpened(final OSNotificationOpenResult result) {
                        LogUtil.loge("notificationOpened");
                        LogUtil.loge("title: "+result.notification.payload.title);
                        LogUtil.loge("body: "+result.notification.payload.body);
                        LogUtil.loge("launchUrl: "+result.notification.payload.launchURL);
                        JSONObject data = result.notification.payload.additionalData;
                        String customKey;
                        if (data != null) {
                            customKey = data.optString("customkey", null);
                            if (customKey != null)
                                LogUtil.loge("customkey set with value: " + customKey);
                            else
                                LogUtil.loge("customkey null");
                        }
                        sendBroadcast(new Intent("close"));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                Intent intent = new Intent(MyApp.this, Home.class);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                if(result.notification.payload.launchURL != null)
                                    intent.putExtra("launchURL", result.notification.payload.launchURL);
                                startActivity(intent);
                            }
                        }, 500);
                    }
                })
                .init();

        if(Build.VERSION.SDK_INT>=24){
            try{
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
    }*/

        private static final String ONESIGNAL_APP_ID = "b4bb6832-3d37-4f77-a3a7-c987a1ac911b";

        @Override
        public void onCreate() {
            super.onCreate();
           // OneSignal.initWithContext(this);
           // OneSignal.setAppId(ONESIGNAL_APP_ID);

        }

}
