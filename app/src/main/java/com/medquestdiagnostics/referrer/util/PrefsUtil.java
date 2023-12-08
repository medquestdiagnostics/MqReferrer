package com.medquestdiagnostics.referrer.util;

import android.content.Context;
import android.content.SharedPreferences;
import com.medquestdiagnostics.referrer.Settings;
import com.google.android.gms.tasks.OnCompleteListener;

public class PrefsUtil {
    private static final String name = "mq_siri_app_preferences";     //"com.app.christbinder12";
    private static final String USER_NAME = "user_name";
    private static final String PASSWORD = "password";
    private static final String IS_RETURNING_INSTANCE = "is_returning_instance";
    private static final String AppLanuches = "AppLanuches3";
    private static final String NoThanksPressed = "NoThanksPressed3";
    private static final String RateNowPressed = "RateNowPressed3";
    private static final String PushEnabled = "PushEnabled";
    private static final String DeviceToken = "DeviceToken";

    private static SharedPreferences getSharedPrefs(Context context){
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public static void setAppLanuches(Context context, int i){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putInt(AppLanuches, i).apply();
    }

    public static int getAppLanuches(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getInt(AppLanuches, 0);
    }

    public static void setNoThanksPressed(Context context, boolean b){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putBoolean(NoThanksPressed, b).apply();
    }

    public static boolean isNoThanksPressed(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getBoolean(NoThanksPressed, false);
    }

    public static void setRateNowPressed(Context context, boolean b){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putBoolean(RateNowPressed, b).apply();
    }
    public static boolean isRateNowPressed(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getBoolean(RateNowPressed, false);
    }

    public static void setPushEnabled(Context context, boolean b){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putBoolean(PushEnabled, b).apply();
        Settings.PUSH_ENABLED = b;
        //OneSignal.setSubscription(b);
    }

    public static void setDeviceToken(Context context, String deviceToken){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(DeviceToken,deviceToken).apply();
        //OneSignal.setSubscription(b);
    }

    public  static String  getDeviceToken(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(DeviceToken,"");
    }

    public static boolean isPushEnabled(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getBoolean(PushEnabled, Settings.PUSH_ENABLED);
    }

    public static void setUserName(Context context, String userName){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(USER_NAME, userName).apply();
    }

    public static String getUserName(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(USER_NAME,null);
    }

    public static void setPassword(Context context, String password){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(PASSWORD, password).apply();
    }

    public static String getPassword(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(PASSWORD,null);
    }

    public static void setIsReturningInstance(Context context, String returningInstance){
        SharedPreferences sp = getSharedPrefs(context);
        sp.edit().putString(IS_RETURNING_INSTANCE, returningInstance).apply();
    }

    public static String getIsReturningInstance(Context context){
        SharedPreferences sp = getSharedPrefs(context);
        return sp.getString(IS_RETURNING_INSTANCE,"false");
    }

    public static void resetPreferences(Context context){

    }

}