package com.medquestdiagnostics.siri;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.app.DownloadManager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.DownloadListener;
import android.webkit.GeolocationPermissions;
import android.webkit.PermissionRequest;
import android.webkit.URLUtil;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebStorage;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.phone.SmsRetriever;
import com.google.android.gms.auth.api.phone.SmsRetrieverClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.medquestdiagnostics.siri.util.AdUtil;
import com.medquestdiagnostics.siri.util.BottomMenuUtil;
import com.medquestdiagnostics.siri.util.DrawerUtil;
import com.medquestdiagnostics.siri.util.FilePicker;
import com.medquestdiagnostics.siri.util.GifUtil;
import com.medquestdiagnostics.siri.util.GpsUtils;
import com.medquestdiagnostics.siri.util.InAppBilling;
import com.medquestdiagnostics.siri.util.InjectUtil;
import com.medquestdiagnostics.siri.util.IntentUtil;
import com.medquestdiagnostics.siri.util.LinkUtil;
import com.medquestdiagnostics.siri.util.LogUtil;
import com.medquestdiagnostics.siri.util.MyJavaScriptInterface;
import com.medquestdiagnostics.siri.util.NetworkUtil;
import com.medquestdiagnostics.siri.util.PermissionUtil;
import com.medquestdiagnostics.siri.util.PrefsUtil;
import com.medquestdiagnostics.siri.util.RateDialogUtil;
import com.medquestdiagnostics.siri.util.SplashUtil;
import com.medquestdiagnostics.siri.util.TimerUtil;
import com.medquestdiagnostics.siri.util.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.concurrent.Executor;

import static android.Manifest.permission.READ_PHONE_NUMBERS;
import static android.Manifest.permission.READ_PHONE_STATE;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.Manifest.permission.RECORD_AUDIO;

public class Home extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.M)

    SwipeRefreshLayout pullToRefresh;
    WebView webView;
    ViewGroup noInternet;
    ViewGroup adParent;
    String mGeoLocationRequestOrigin = null;
    GeolocationPermissions.Callback mGeoLocationCallback = null;
    PermissionRequest permissionRequest;
    public ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> mFilePathCallback;
    String userId = "";
    String deviceToken = "";
    boolean showingLocalWebsite = false;
    ViewGroup container;
    ViewGroup gifAnim;
    ImageView gifAnimImg;
    private WebView mWebviewPop;
    private final String target_url_prefix = Settings.HOST;
    TimerUtil timerUtil;
    DrawerUtil drawerUtil;
    InAppBilling inAppBilling;

    private NotificationManager mNotifyManager;
    private Notification mBuilder;

    public static String currentUrl = Settings.HOME_URL;
    public static boolean showRateDialog;
    public static int linkClicks = 0;
    public static boolean homeLoaded = false;
    public static Uri mPhotoCapturedURI;
    public static final int LOC_PERMISSION = 100;
    public static final int VIDEO_CAPTURE_PERMISSION1 = 1001;
    public static final int VIDEO_CAPTURE_PERMISSION2 = 1002;
    public static final int DOWNLOAD_PERMISSION = 102;
    public static final int READ_PERMISSION = 0;
    public static final int WRITE_PERMISSION = 0;
    public static final int CAMERA_PERMISSION = 0;
    public static final int AUDIO_PERMISSION = 106;
    public static final int READ_PHONE_NUMBER_PERMISSION = 107;
    public final static int FILECHOOSER_RESULTCODE = 1;
    public String  savedtoken = "";
    public String  isFromNoti = "";
//    private boolean IsLoginAttempted = false;
    private MySMSBroadcastReceiver mySMSBroadcastReceiver;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Utils.setScreenOrientation(this);
        Utils.disableScreenCapture(Home.this);

        FirebaseMessaging.getInstance().setAutoInitEnabled(true);

        //String diviceToken = FirebaseMessaging.getInstance().getToken().toString();
        FirebaseMessaging.getInstance().subscribeToTopic("all");
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            callService();
                            return;
                        }
                        // Get new FCM registration token
                        String token = task.getResult();

                        LogUtil.loge("@@@ Device Token : " + token);
                        deviceToken = token;

                        callService();
                    }
                });

         savedtoken = PrefsUtil.getDeviceToken(this);


        // String no = getMy10DigitPhoneNumber();

//        if (!Settings.SHOW_DRAWER && !Settings.SHOW_BOTTOM_MENU) {
//            setContentView(R.layout.activity_home);
//            init();
//        } else {
//            setContentView(R.layout.activity_home_drawer);
//            init();
//        }
//
//        if (Settings.SHOW_DRAWER) {
//            drawerUtil = new DrawerUtil(this);
//            drawerUtil.setupDrawer();
//        }
//        if (Settings.SHOW_BOTTOM_MENU) {
//            BottomMenuUtil.setBottomMenu(this);
//        }
//
//        if (Settings.SHOW_GIF_ANIMATION) {
//            GifUtil.showGifAnimation(this, gifAnim, gifAnimImg);
//        } else {
//            GifUtil.hideGifAnimation(gifAnim);
//        }
//
//        Settings.PUSH_ENABLED = PrefsUtil.isPushEnabled(this);
//
//        setInitialUrl();
//        LogUtil.loge("url: " + currentUrl);
//
//        setStatusBarColor();
//        setPulltoRefresh();
//        setupWebView(webView, false);
//        loadWebPage();
//        if (Settings.SPLASH_SCREEN) {
//            SplashUtil.showSplash(this);
//        }
//
//        PrefsUtil.setAppLanuches(this, PrefsUtil.getAppLanuches(this) + 1);
//
//        if (Settings.RATE_DIALOG) {
//            showRateDialog = RateDialogUtil.shouldShow(this);
//        }
//
//        AdUtil.loadBannerAd(this, adParent);
//
//        inAppBilling = new InAppBilling();
//        inAppBilling.setupBillingClient(this);
//        // check latest url in webview every second
//        timerUtil = new TimerUtil();
//        timerUtil.startTimer(webView);
//        registerReceiver(CloseHome, new IntentFilter("close"));

        requestPermission();
        requestLocPermission();
      //  GetNumber();

        new GpsUtils(this).turnGPSOn(new GpsUtils.onGpsListener() {
            @Override
            public void gpsStatus(boolean isGPSEnable) {
                // turn on GPS
            }
        });
    }

    @Override
    public void onNewIntent(Intent intent){
        super.onNewIntent(intent);

        Bundle extras = intent.getExtras();
      //  Toast.makeText(Home.this, "On new extras " + extras , Toast.LENGTH_LONG).show();
        if(extras != null){
           isFromNoti = extras.getString("FromNotification");
            String clickAction = extras.getString("clickAction");
           // Toast.makeText(Home.this, "On New intent " + name + vl , Toast.LENGTH_SHORT).show();
            if(isFromNoti.equalsIgnoreCase("77") ){
                if(clickAction != null && clickAction.length()>0) {
                    currentUrl = clickAction;
                } else {
                    currentUrl = Settings.NOTIFICATION_URL;
                }
                loadWebPage();
            }
        }

    }

    @Override
    protected void onResume() {
        Bundle extras = getIntent().getExtras();
        System.out.println(extras+"");
     //   Toast.makeText(Home.this, "On resume extras " + extras , Toast.LENGTH_SHORT).show();

        if (extras != null) {
            try {
                onNewIntent(getIntent());
                isFromNoti = getIntent().getStringExtra("FromNotification");
               // Toast.makeText(Home.this, "On resume " + vl , Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        super.onResume();
    }

//    @Override
//    protected void onStart() {
//
//    }

    public void callService() {

        if (!Settings.SHOW_DRAWER && !Settings.SHOW_BOTTOM_MENU) {
            setContentView(R.layout.activity_home);
            init();
        } else {
            setContentView(R.layout.activity_home_drawer);
            init();
        }

        if (Settings.SHOW_DRAWER) {
            drawerUtil = new DrawerUtil(this);
            drawerUtil.setupDrawer();
        }
        if (Settings.SHOW_BOTTOM_MENU) {
            BottomMenuUtil.setBottomMenu(this);
        }

        if (Settings.SHOW_GIF_ANIMATION) {
            GifUtil.showGifAnimation(this, gifAnim, gifAnimImg);
        } else {
            GifUtil.hideGifAnimation(gifAnim);
        }

        Settings.PUSH_ENABLED = PrefsUtil.isPushEnabled(this);
        String existingToken = PrefsUtil.getDeviceToken(this);

        if (existingToken != deviceToken) {
            PrefsUtil.setDeviceToken(this, deviceToken);
        }

        savedtoken = PrefsUtil.getDeviceToken(this);

        setInitialUrl();
        LogUtil.loge("url: " + currentUrl);
        setStatusBarColor();
        setPulltoRefresh();
        setupWebView(webView, false);
        loadWebPage();
        if (Settings.SPLASH_SCREEN) {
            SplashUtil.showSplash(this);
        }

        PrefsUtil.setAppLanuches(this, PrefsUtil.getAppLanuches(this) + 1);

        if (Settings.RATE_DIALOG) {
            showRateDialog = RateDialogUtil.shouldShow(this);
        }

        AdUtil.loadBannerAd(this, adParent);

        inAppBilling = new InAppBilling();
        inAppBilling.setupBillingClient(this);
        // check latest url in webview every second
        timerUtil = new TimerUtil();
        timerUtil.startTimer(webView);
        registerReceiver(CloseHome, new IntentFilter("close"));

        startSMSRetrieverClient();
    }

    public void GetNumber() {

        if (ActivityCompat.checkSelfPermission(this, READ_PHONE_NUMBERS) ==
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
                READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            // Permission check

            // Create obj of TelephonyManager and ask for current telephone service
            TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
            String phoneNumber = telephonyManager.getLine1Number();

            LogUtil.loge("Phone number: " + phoneNumber);

           // phone_number.setText(phoneNumber);
            return;
        } else {
            // Ask for permission
           // requestPhoneNumberPermission();

        }
    }

    private void requestPhoneNumberPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{READ_PHONE_NUMBERS, READ_PHONE_STATE}, READ_PHONE_NUMBER_PERMISSION);
        }
    }
    private void requestLocPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, LOC_PERMISSION);
        }
    }
    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(this,
                    READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                   ActivityCompat.checkSelfPermission(this,
                    WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permission check

                // Create obj of TelephonyManager and ask for current telephone service

                LogUtil.loge("PERMISSION GRANTED ");
                GetNumber();

                // phone_number.setText(phoneNumber);
                return;
            } else {
                // Ask for permission
                requestPermissions(new String[]{CAMERA,READ_EXTERNAL_STORAGE,WRITE_EXTERNAL_STORAGE,RECORD_AUDIO}, WRITE_PERMISSION);

            }
        }
    }



//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
//       super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        switch (requestCode) {
//            case READ_PHONE_NUMBER_PERMISSION:
//                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//                if (ActivityCompat.checkSelfPermission(this, READ_SMS) !=
//                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this,
//                        READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
//                        ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//                    return;
//                }
//                String phoneNumber = telephonyManager.getLine1Number();
//                LogUtil.loge("Phone number: " + phoneNumber);
//
//                // phone_number.setText(phoneNumber);
//                break;
//            default:
//                throw new IllegalStateException("Unexpected value: " + requestCode);
//        }
//    }



//    private String getMyPhoneNumber() {
//        TelephonyManager mTelephonyMgr;
//        mTelephonyMgr = (TelephonyManager)
//                getSystemService(Context.TELEPHONY_SERVICE);
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            // TODO: Consider calling
//            ///  String mPhoneNumber = mTelephonyMgr.getLine1Number();
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return "Permission not granted";
//        }
//        String mPhoneNumber = mTelephonyMgr.getLine1Number();
//
//        LogUtil.loge("phone number : " + getMyPhoneNO());
//        return mPhoneNumber;
//
//    }




//    private boolean checkPermission() {
//        int writePerm =   ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE);
//        int readPerm = ContextCompat.checkSelfPermission(context,Manifest.permission.READ_EXTERNAL_STORAGE);
//        return writePerm== PackageManager.PERMISSION_GRANTED && readPerm==PackageManager.PERMISSION_GRANTED;
//    }

    //@Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode==Constants.PERMISSION_REQUEST_CODE){
//            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED
//                    && grantResults[1]==PackageManager.PERMISSION_GRANTED){
//                // do Operation
//            }
//            else{
//                Toast.makeText(context,"Permission Denied",Toast.LENGTH_SHORT).show();
//            }
//        }
//    }


//    private String getMy10DigitPhoneNumber(){
//        String s = getMyPhoneNumber();
//        return s != null && s.length() > 2 ? s.substring(2) : null;
//    }

    private void init() {
        container = findViewById(R.id.container);
        webView = findViewById(R.id.webView);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh = findViewById(R.id.pullToRefresh);
        }
        adParent = findViewById(R.id.adParent);
        noInternet = findViewById(R.id.noInternet);
        gifAnim = findViewById(R.id.gifAnim);
        gifAnimImg = findViewById(R.id.gifAnimImg);
    }

    public void webviewGoBack() {
        if(webView.canGoBack()){
            webView.goBack();
        }
    }

    private void setInitialUrl() {
        if(getIntent().hasExtra("launchURL")){
            currentUrl = getIntent().getStringExtra("launchURL");
            return;
        }

        if(!Settings.OPEN_LOCAL_HTML_BY_DEFAULT){
            setOnlineHomeUrl();
            String strData = LinkUtil.checkUniversalLink(this);
            checkOpenSpecialLinkOnline(strData);
        }
        else{
            setOfflineHomeUrl();
            String strData = LinkUtil.checkUniversalLink(this);
            checkOpenSpecialLinkOffline(strData);
        }
    }

    private void setOnlineHomeUrl() {
        showingLocalWebsite = false;
        currentUrl = Settings.HOME_URL;
        if (Settings.PUSH_ENABLED) {
            // get push token
           // OSPermissionSubscriptionState status = OneSignal.getPermissionSubscriptionState();
            //userId = status.getSubscriptionStatus().getUserId();
            LogUtil.loge("deviceToken: "+deviceToken);



                if(isFromNoti.equalsIgnoreCase("77") ){
                    currentUrl = Settings.NOTIFICATION_URL;
                    ///  LogUtil.loge("is from notification "+fromnotification);
                 }

            if(currentUrl.contains("?")){
                currentUrl = currentUrl + "&player_id=" + savedtoken;
            }
            else {
                currentUrl = currentUrl + "?player_id=" + savedtoken;
            }
            LogUtil.loge("full url: "+currentUrl);
        }
        webView.clearHistory();
    }

    private void setOfflineHomeUrl() {
        showingLocalWebsite = true;
        currentUrl = "file:///android_asset/website/"+Settings.LOCAL_HTML_INDEX_PAGE;
        webView.clearHistory();
    }

    private void checkOpenSpecialLinkOnline(String strData){
        if(strData == null)
            return;
        String [] arr = strData.split("//");
        if(arr.length > 1){
            // open online specific page
            currentUrl = Settings.HOME_URL
                    .replace("?","")
                    .replace("/saifal.php","")
                    +"/"+arr[1];
        }
        else{
            // open online home page
            currentUrl = Settings.HOME_URL;
        }
        showingLocalWebsite = false;
    }

    private void checkOpenSpecialLinkOffline(String strData){
        if(strData == null)
            return;
        String [] arr = strData.split("//");
        if(arr.length > 1){
            // open offline specific page
            currentUrl = "file:///android_asset/website/"+arr[1];
        }
        else{
            // open offline home page
            currentUrl = "file:///android_asset/website/"+Settings.LOCAL_HTML_INDEX_PAGE;
        }
        showingLocalWebsite = true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void showContent(){
        if(Settings.SHOW_DRAWER){
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.setVisibility(View.VISIBLE);
        }
        if(!pullToRefresh.isShown())
            pullToRefresh.setVisibility(View.VISIBLE);
    }

    public void hideContent(){
        if(Settings.SHOW_DRAWER){
            DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
            drawerLayout.setVisibility(View.GONE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh.setVisibility(View.GONE);
        }
    }

    private void setStatusBarColor() {
        Utils.changeStatusBarColor(this, getResources().getColor(R.color.statusBarColor));
    }

    private void setPulltoRefresh() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh.setEnabled(Settings.PULL_TO_REFRESH);
        }
        int color = getResources().getColor(R.color.loadingSignColor);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh.setColorSchemeColors(color, color, color);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh.setProgressViewOffset(false, 0 , (int)Utils.convertDpToPixel(Settings.PULL_TO_REFRESH_POSITION_FROM_TOP, this));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    LogUtil.loge("onRefresh");
                    loadWebPage();
                }
            });
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                @Override
                public void onScrollChanged() {
                    if (webView.getScrollY() == 0)
                        pullToRefresh.setEnabled(Settings.PULL_TO_REFRESH);
                    else
                        pullToRefresh.setEnabled(false);
                }
            });
        }
    }

    private void setupWebView(WebView webView, boolean isPopup) {

//        if(!Settings.USER_AGENT.isEmpty()) {
//            webView.getSettings().setUserAgentString(Settings.USER_AGENT);
//        }
//
//        webView.getSettings().setJavaScriptEnabled(true);
//        webView.getSettings().setLoadWithOverviewMode(true);
//        webView.getSettings().setUseWideViewPort(true);
//        webView.getSettings().setSupportZoom(true);
//        webView.getSettings().setBuiltInZoomControls(false);
//        webView.getSettings().setAllowFileAccess(true);
//        webView.getSettings().setAllowFileAccessFromFileURLs(true);
//        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
//        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
//        webView.getSettings().setDomStorageEnabled(true);
//        webView.getSettings().setSupportMultipleWindows(true);
//        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
////        webView.getSettings().setAppCacheEnabled(Settings.CACHE_ENABLED); //Temp
//        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
//        webView.setWebViewClient(new MyWebViewClient(isPopup));
//        webView.setWebChromeClient(new MyWebChromeClient());
//        webView.setDownloadListener(new TestDownloadManager());
//        webView.addJavascriptInterface(new MyJavaScriptInterface(Home.this), "Android");

        if(!Settings.USER_AGENT.isEmpty())
            webView.getSettings().setUserAgentString(Settings.USER_AGENT);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(false);
        webView.getSettings().setLoadWithOverviewMode(true);
        webView.getSettings().setUseWideViewPort(true);
        webView.getSettings().setAllowFileAccess(true);
        webView.getSettings().setAllowFileAccessFromFileURLs(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
        webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setSupportMultipleWindows(true);
        webView.getSettings().setPluginState(WebSettings.PluginState.ON);
//        webView.getSettings().setAppCacheEnabled(Settings.CACHE_ENABLED);
        webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        webView.setWebViewClient(new MyWebViewClient(isPopup));
        webView.setWebChromeClient(new MyWebChromeClient());
        webView.setDownloadListener(new TestDownloadManager());
        webView.addJavascriptInterface(new MyJavaScriptInterface(Home.this), "Android");
    }

    String downloadUrl = "";
    String contentDisposition = "";
    String mimeType = "";
    private long downloadReference;

    private class TestDownloadManager implements DownloadListener {
        public void onDownloadStart(String url, String userAgent, String contDis, String mtype, long contentLength) {
            LogUtil.loge("onDownloadStart: "+url);
            downloadUrl = url;
            contentDisposition = contDis;
            mimeType = mtype;
            if(PermissionUtil.checkDownloadPermission(Home.this)) {
//                  try {
//                        downloadFile();
//                    } catch (IOException e) {
//                        e.printStackTrace();
//                    }
                if (url.startsWith("data:")) {  //when url is base64 encoded data
                    String path = createAndSaveFileFromBase64Url(url);
                    return;
                }else {
                    try {
                        downloadFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            else{
                PermissionUtil.getDownloadPermission(Home.this);
            }
        }
    }

    public String createAndSaveFileFromBase64Url(String url) {

        File path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        String filetype = url.substring(url.indexOf("/") + 1, url.indexOf(";"));
        String filename = System.currentTimeMillis() + "." + filetype;
        File file = new File(path, filename);
        try {
            if(!path.exists())
                path.mkdirs();
            if(!file.exists())
                file.createNewFile();

            String base64EncodedString = url.substring(url.indexOf(",") + 1);
            byte[] decodedBytes = Base64.decode(base64EncodedString, Base64.DEFAULT);
            OutputStream os = new FileOutputStream(file);
            os.write(decodedBytes);
            os.close();
            LogUtil.loge("file path url: "+file);

//            FileOutputStream fos = new FileOutputStream(storagePath);
//            fos.write(Base64.decode(base64EncodedString, Base64.NO_WRAP));
//            fos.close();
          //  Toast.makeText(this, R.string.downloading_success, Toast.LENGTH_LONG).show();

            //Tell the media scanner about the new file so that it is immediately available to the user.
            MediaScannerConnection.scanFile(this,
                    new String[]{file.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            Uri data = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider" ,file);


            //Set notification after download complete and add "click to view" action to that
            String mimetype = url.substring(url.indexOf(":") + 1, url.indexOf("/"));
            Intent intent = new Intent(Intent.ACTION_VIEW);
           // intent.setAction(android.content.Intent.ACTION_VIEW);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            //intent.setDataAndType(Uri.fromFile(file), (mimetype + "/*"));
            intent.setDataAndType(data, "/*");
           // intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            mBuilder = new Notification.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentText(getString(R.string.downloading))
                    .setContentTitle(filename)
                    .setContentIntent(pIntent)
                    .build();

            mBuilder.flags |= Notification.FLAG_AUTO_CANCEL;
            int notificationId = 85851;
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(notificationId, mBuilder);

        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + file, e);
            Toast.makeText(getApplicationContext(), R.string.downloading_failed, Toast.LENGTH_LONG).show();
        }

        return file.toString();
    }
    private void downloadFile() throws IOException {
        if(downloadUrl.isEmpty())
            return;

        if((downloadUrl != null && downloadUrl.length()>0) && downloadUrl.contains("blob:")) {

            webView.post(new Runnable() {
                @Override
                public void run() {
                    String pdfUrlHiddenJs = "javascript:document.getElementById('pdf_url_hidden').value;";
                    webView.evaluateJavascript(pdfUrlHiddenJs, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {

                            if (value != null && value.length() > 0) {
                                value = value.replaceAll("\"", "");
                            }

                            if (value != null && value.length() > 0) {
                                if (value.equalsIgnoreCase("null")) {
                                } else {
                                    downloadUrl = value;

                                    LogUtil.loge("download url: " + downloadUrl);
                                    Toast.makeText(Home.this, getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();

                                    DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                                    DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
                                    String fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType);
                                    LogUtil.loge("fileName: " + fileName);
                                    request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                                    request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                                    request.setTitle("" + fileName);
                                    request.setDescription("Downloading");
                                    request.setMimeType(mimeType);
                                    downloadReference = downloadManager.enqueue(request);
                                    IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
                                    registerReceiver(downloadReceiver, filter);

                                }
                            }
                        }
                    });

                }
            });

        } else {

            LogUtil.loge("download url: " + downloadUrl);
            Toast.makeText(Home.this, getResources().getString(R.string.downloading), Toast.LENGTH_SHORT).show();

            DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
            String fileName = URLUtil.guessFileName(downloadUrl, contentDisposition, mimeType);
            LogUtil.loge("fileName: " + fileName);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("" + fileName);
            request.setDescription("Downloading");
            request.setMimeType(mimeType);
            downloadReference = downloadManager.enqueue(request);
            IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
            registerReceiver(downloadReceiver, filter);

        }
    }

    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //check if the broadcast message is for our enqueued download
            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            if (referenceId == downloadReference) {
                LogUtil.loge("open file");
                IntentUtil.openDownloadedAttachment(context, referenceId);
                unregisterReceiver(this);
            }
        }
    };

    public void loadWebPageFromBottomMenu(String url) {
        LogUtil.loge("loadWebPageFromBottomMenu: "+url);
        currentUrl = url;
        if(currentUrl != null) {
            if(!currentUrl.isEmpty()) {

                showProgress();
                webView.loadUrl(currentUrl);
            }
        }
    }

    public void loadWebPageFromDrawer(String url) {
        LogUtil.loge("loadWebPageFromDrawer: "+url);
        drawerUtil.drawerToggle();
        currentUrl = url;
        if(currentUrl != null) {
            if(!currentUrl.isEmpty()) {
                showProgress();
                webView.loadUrl(currentUrl);
            }
        }
    }

    public void loadWebPage() {
        if(currentUrl != null) {
            if(!currentUrl.isEmpty()) {
                LogUtil.loge("loadWebPage: "+currentUrl);
                if(homeLoaded)
                    showProgress();
                webView.loadUrl(currentUrl);
            }
        }
    }

    public String updateUrl(String url){
        if(url.contains("?")){
            url = url + "&player_id=" + savedtoken;
        }
        else {
            url = url + "?player_id=" + savedtoken;
        }
        return  url;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void grantedVideoCapturePermission() {
        LogUtil.loge("grantedVideoCapturePermission");
        permissionRequest.grant(permissionRequest.getResources());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == LOC_PERMISSION) {
                if (mGeoLocationCallback != null)
                    mGeoLocationCallback.invoke(mGeoLocationRequestOrigin, true, true);
            }
            else if(requestCode == VIDEO_CAPTURE_PERMISSION1){
                if(!PermissionUtil.checkAudioPermission(Home.this)){
                    PermissionUtil.getAudioPermission(Home.this);
                }
                else{
                    grantedVideoCapturePermission();
                }
            }
            else if(requestCode == VIDEO_CAPTURE_PERMISSION2){
                grantedVideoCapturePermission();
            }
            else if(requestCode == DOWNLOAD_PERMISSION){
               // downloadFile();
            }
            else if(requestCode == READ_PERMISSION){
               // FilePicker.openChooser(Home.this);
            }
            else if(requestCode == WRITE_PERMISSION){
               // FilePicker.openChooser(Home.this);
            }
            else if(requestCode == CAMERA_PERMISSION){
               // FilePicker.openChooser(Home.this);
            }
            else if (requestCode == READ_PHONE_NUMBER_PERMISSION){
                TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
                if ( ActivityCompat.checkSelfPermission(this,
                        READ_PHONE_NUMBERS) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(this, READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                String phoneNumber = telephonyManager.getLine1Number();
                LogUtil.loge("Phone number: " + phoneNumber);
            }
        }
    }

    private class MyWebChromeClient extends WebChromeClient{
        @Override
        public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message resultMsg) {
            LogUtil.loge("onCreateWindow");
            LogUtil.loge("isDialog: "+isDialog);
            LogUtil.loge("isUserGesture: "+isUserGesture);
//            if(!isDialog){
//                 //_blank link
//                WebView.HitTestResult result = view.getHitTestResult();
//                String data = result.getExtra();
//                if(data != null){
//                    currentUrl = data;
//                    loadWebPage();
//                }
//                else{
//                    LogUtil.loge("data NULL");
//                }
//                return false;
//            }
            mWebviewPop = new WebView(Home.this);
            setupWebView(mWebviewPop, true);
            mWebviewPop.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            container.addView(mWebviewPop);
            WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
            transport.setWebView(mWebviewPop);
            resultMsg.sendToTarget();
            return true;
        }

        @Override
        public void onCloseWindow(WebView window) {
            LogUtil.loge("onCloseWindow");
        }

        @Override
        public void onGeolocationPermissionsShowPrompt(String origin, GeolocationPermissions.Callback callback) {
            LogUtil.loge("onGeolocationPermissionsShowPrompt");
            if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOC_PERMISSION);
                mGeoLocationRequestOrigin = origin;
                mGeoLocationCallback = callback;
            }
            else{
                callback.invoke(origin, true, true);
            }
        }

        @Override
        public void onPermissionRequest(PermissionRequest request) {
            LogUtil.loge("onPermissionRequest");
            permissionRequest = request;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                for(String permission: request.getResources()){
                    if(permission.equals(PermissionRequest.RESOURCE_VIDEO_CAPTURE)){
                        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.CAMERA}, VIDEO_CAPTURE_PERMISSION1);
                        }
                        else if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(Home.this, new String[]{Manifest.permission.RECORD_AUDIO}, VIDEO_CAPTURE_PERMISSION2);
                        }
                        else{
                            grantedVideoCapturePermission();
                        }
                    }
                }
            }
        }

        // For Lollipop 5.0+ Devices
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
            //String acceptTypes[] = fileChooserParams.getAcceptTypes();
            LogUtil.loge("onShowFileChooser");
            if (mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            LogUtil.loge("mFilePathCallback not nil");
            mFilePathCallback = filePathCallback;
            FilePicker.openChooser(Home.this);
            return true;
        }

        //For Android 4.1 only
        protected void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            LogUtil.loge("types: "+acceptType);
            mUploadMessage = uploadMsg;
            FilePicker.openChooser(Home.this);
        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {

            mUploadMessage = uploadMsg;
            FilePicker.openChooser(Home.this);
        }

        // full screen video
        private View mCustomView;
        private WebChromeClient.CustomViewCallback mCustomViewCallback;
        protected FrameLayout mFullscreenContainer;
        private int mOriginalOrientation;
        private int mOriginalSystemUiVisibility;

        public Bitmap getDefaultVideoPoster() {
            return BitmapFactory.decodeResource(Home.this.getApplicationContext().getResources(), 2130837573);
        }

        public void onHideCustomView() {
            ((FrameLayout)Home.this.getWindow().getDecorView()).removeView(this.mCustomView);
            this.mCustomView = null;
            Home.this.getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
            Home.this.setRequestedOrientation(this.mOriginalOrientation);
            this.mCustomViewCallback.onCustomViewHidden();
            this.mCustomViewCallback = null;
        }

        public void onShowCustomView(View paramView, WebChromeClient.CustomViewCallback paramCustomViewCallback) {
            if (this.mCustomView != null) {
                onHideCustomView();
                return;
            }
            this.mCustomView = paramView;
            this.mOriginalSystemUiVisibility = Home.this.getWindow().getDecorView().getSystemUiVisibility();
            this.mOriginalOrientation = Home.this.getRequestedOrientation();
            this.mCustomViewCallback = paramCustomViewCallback;
            ((FrameLayout)Home.this.getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
            Home.this.getWindow().getDecorView().setSystemUiVisibility(3846);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult r = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(r != null) {
            if(r.getContents() == null) {
                //
            }
            else {
                String scanned = r.getContents();
                Toast.makeText(this, "Scanned: "+scanned, Toast.LENGTH_LONG).show();
                if (!Settings.OPEN_SCAN_URL_IN_WEBVIEW) {
                    Utils.openUrlInChrome(Home.this, scanned);
                }
                else{
                    currentUrl = scanned;
                    loadWebPage();
                }
            }
        }

        // when photo is selected gallery/camera
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode != FILECHOOSER_RESULTCODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            Uri[] results = null;
            // Check that the response is a good one
            if (resultCode == Activity.RESULT_OK) {
                if (data == null) {
                    if (mPhotoCapturedURI != null) {
                        results = new Uri[]{mPhotoCapturedURI};
                        LogUtil.loge("camera results: "+results[0]);
                    }
                }
                else {
                    if(data.getData() == null){
                        if (mPhotoCapturedURI != null) {
                            results = new Uri[]{mPhotoCapturedURI};
                            LogUtil.loge("camera results: "+results[0]);
                        }
                    }
                    else{
                        String dataString = data.getDataString();
                        if (dataString != null) {
                            results = new Uri[]{Uri.parse(dataString)};
                            LogUtil.loge("gallery results: "+results[0]);
                        }
                    }
                }
            }
            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
        }
        else if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            if (requestCode != FILECHOOSER_RESULTCODE || mUploadMessage == null) {
                super.onActivityResult(requestCode, resultCode, data);
                return;
            }
            if (requestCode == FILECHOOSER_RESULTCODE) {
                if (null == this.mUploadMessage) {
                    return;
                }
                Uri result = null;
                try {
                    if (resultCode != RESULT_OK) {
                        result = null;
                    } else {
                        // retrieve from the private variable if the intent is null
                        result = data == null ? mPhotoCapturedURI : data.getData();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), "activity :" + e, Toast.LENGTH_LONG).show();
                }
                mUploadMessage.onReceiveValue(result);
                mUploadMessage = null;
            }
        }
    }

    private class MyWebViewClient extends WebViewClient {
        boolean isPopup;

        public MyWebViewClient(boolean isPopup){
            this.isPopup = isPopup;
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            String url = request.getUrl().toString();
            url = updateUrl(url);
            LogUtil.loge("@@@ shouldOverrideUrlLoading1: " + url);
            return handleOverrideUrl(url);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            LogUtil.loge("@@@ shouldOverrideUrlLoading2: " + url);
            return handleOverrideUrl(url);
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
            LogUtil.loge("@@@ onPageStarted: " + url);
            currentUrl = url;

            if(homeLoaded) {
                LogUtil.loge("show progress");
                showProgress();
            }

            if(!NetworkUtil.isInternetOn(Home.this)) {
                showNoInternet(true);
                hideProgress();
            }
            else {
                showNoInternet(false);
            }
        }

        @Override
        public void doUpdateVisitedHistory(WebView view, String url, boolean isReload) {
            super.doUpdateVisitedHistory(view, url, isReload);
            LogUtil.loge("doUpdateVisitedHistory: "+url);
            currentUrl = url;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            LogUtil.loge("finished: " + url);
            //String cookies = CookieManager.getInstance().getCookie(url);
            //LogUtil.loge("cookies:" + cookies);
            hideProgress();

            if (url.contains(target_url_prefix)) {
                LogUtil.loge("1");
                if(!isPopup)
                    closeWebviewPopup();
            }

            if(url.endsWith("/logout")){
                LogUtil.loge("2");
                PrefsUtil.resetPreferences(Home.this);
//                IsLoginAttempted = false;

                if(!isPopup)
                    closeWebviewPopup();
                setInitialUrl();
                loadWebPage();
                return;
            }

            if(Settings.INJECT_CSS)
                InjectUtil.injectCSS(Home.this, view, "website/css/"+Settings.INJECT_CSS_FILENAME);
            if(Settings.INJECT_JS)
                InjectUtil.injectScriptFile(Home.this, view, "website/js/"+Settings.INJECT_JS_FILENAME);

            if(Settings.SHOW_DRAWER){
                if(webView.canGoBack()){
                    drawerUtil.showBack();
                }
                else{
                    drawerUtil.hideBack();
                }
            }

            // check if home url loaded (1st url)
            if(!homeLoaded){
                homeLoaded = true;
            }

            if(url.contains("/login.html")){

                String IsReturningInstance = PrefsUtil.getIsReturningInstance(Home.this);

                LogUtil.loge("@@@ IsReturningInstance..." + IsReturningInstance);// + "...IsLoginAttempted..." + IsLoginAttempted);
                LogUtil.loge("@@@ PrefsUtil user_name..." + PrefsUtil.getUserName(Home.this));
                LogUtil.loge("@@@ PrefsUtil password..." + PrefsUtil.getPassword(Home.this));

                if(IsReturningInstance.equalsIgnoreCase("true")) {//&& (IsLoginAttempted == false)) {
//                    IsLoginAttempted = true;
                    String otpButtonJs = "document.getElementsByClassName('otp_login_div')[0].style.display;";
                    webView.evaluateJavascript(otpButtonJs, new ValueCallback<String>() {
                        @Override
                        public void onReceiveValue(String value) {
                            // 'value' contains the result of the JavaScript code
                            LogUtil.loge("@@@ otpButtonStatus ..." + value);

                            if(value != null && value.length()>0) {
                                value = value.replaceAll("\"", "");
                            }

                            if(value != null && value.length()>0) {
                                if(value.equalsIgnoreCase("null")) {

                                } else if(value.equalsIgnoreCase("none")) {
                                    checkAndLaunchBioMetric();
                                }
                            }
                        }
                    });

                }

                loadEvent(clickListener());

            } else if(url.contains("/home")){
                PrefsUtil.setIsReturningInstance(Home.this, "true");
//                IsLoginAttempted = false;

            } else if(url.contains("/index")){
                String userName = PrefsUtil.getUserName(Home.this);
                if(userName != null && userName.length()>0) {
                    PrefsUtil.setIsReturningInstance(Home.this, "true");
                }
            }

        }

        private boolean handleOverrideUrl(String url) {
            linkClicks++;
            if(linkClicks >= Settings.INTERSTITIAL_AD_AFTER_CLICKS){
                //AdUtil.loadInterstitialAd(Home.this);
            }
            String host = Uri.parse(url).getHost();
            LogUtil.loge("host: "+host);

            if(url.startsWith("qrcode://")){
                openQrScanner();
                return true;
            }
            else if(url.startsWith("ratedialog://")){
                RateDialogUtil.showRateDialog(Home.this);
                return true;
            }
            else if(url.startsWith("reset://")){
                clearCache();
                return true;
            }
            else if(url.startsWith("enablepush://")){
                PrefsUtil.setPushEnabled(Home.this, true);
                return true;
            }
            else if(url.startsWith("disablepush://")){
                PrefsUtil.setPushEnabled(Home.this, false);
                return true;
            }
            else if(url.startsWith("inapp://")){
                String [] arr = url.split("//");
                if(arr.length > 1){
                    String productId = arr[1];
                    inAppBilling.makePurchase(productId);
                }
                return true;
            }
            else if(url.startsWith(getString(R.string.universalLink))){
                if(!Settings.OPEN_LOCAL_HTML_BY_DEFAULT){
                    checkOpenSpecialLinkOnline(url);
                }
                else{
                    checkOpenSpecialLinkOffline(url);
                }
                loadWebPage();
                return true;
            }
            else if(!url.startsWith("file:///android_asset")
                    && !url.startsWith("http")
                    && !url.startsWith("https")) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
            else if(url.contains("play.google.com")){
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                return true;
            }
            else if(url.startsWith("https://web.facebook.com/dialog/close_window")){
                setInitialUrl();
                loadWebPage();
                return true;
            }
            else if(url.startsWith("https://accounts.google.com/signin")){
                LogUtil.loge("3");
                if(!isPopup)
                    closeWebviewPopup();
                return false;
            }
            else if (host.contains(target_url_prefix)) {
                LogUtil.loge("4");
                if(!isPopup)
                    closeWebviewPopup();
                return false;
            }

            if(!Settings.OPEN_LOCAL_HTML_BY_DEFAULT) {
                if (!url.contains(Settings.HOST)) {
                    if (Settings.OPEN_EXTERNAL_LINKS_IN_BROWSER) {
                        for (String whitelistExternalUrl : Settings.WHITELIST_EXTERNAL_URLS) {
                            if(url.equals(whitelistExternalUrl)){
                                return false;
                            }
                        }
                        Utils.openUrlInChrome(Home.this, url);
                        return true;
                    }
                }
            }
            return false;
        }

    }

    private void loadEvent(String javascript){
        webView.loadUrl("javascript:"+javascript);
    }

    private String clickListener(){
        return getButton()+ "button.onclick = function(){ " +  getUserDetails() + "  };";
    }

    private String getButton(){
        return "var button = document.getElementById('siri_login');";
    }

    private String getUserDetails(){
        webView.post(new Runnable() {
            @Override
            public void run() {
                String userNameJs = "document.getElementsByName('user_name')[0].value;";
                webView.evaluateJavascript(userNameJs, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {
                        // 'value' contains the result of the JavaScript code

                        if(value != null && value.length()>0) {
                            value = value.replaceAll("\"", "");
                        }

                        if(value != null && value.length()>0) {
                            if(value.equalsIgnoreCase("null")) {
                                LogUtil.loge("@@@ onReceiveValue user_name null value...");
                            } else {
                                PrefsUtil.setUserName(Home.this, value);
                            }
                        }
                    }
                });

                String passwordJs = "document.getElementsByName('user_password')[0].value;";
                webView.evaluateJavascript(passwordJs, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String value) {

                        if(value != null && value.length()>0) {
                            value = value.replaceAll("\"", "");
                        }

                        if(value != null && value.length()>0) {
                            if(value.equalsIgnoreCase("null")) {
                                LogUtil.loge("@@@ onReceiveValue user_password null value...");
                            } else {
                                PrefsUtil.setPassword(Home.this, value);
                            }
                        }
                    }
                });
            }
        });

        return null;
    }

    private void closeWebviewPopup() {
        if (mWebviewPop != null) {
            LogUtil.loge("remove webview popup");
            mWebviewPop.setVisibility(View.GONE);
            container.removeView(mWebviewPop);
            mWebviewPop = null;
        }
    }

    private void openQrScanner() {
        new IntentIntegrator(this).initiateScan();
    }

    public void clearCache() {
        LogUtil.loge("clearCache");
        WebStorage.getInstance().deleteAllData();
        webView.clearCache(true);
        webView.clearFormData();
        webView.clearHistory();
        webView.clearSslPreferences();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        }
        else {
            CookieSyncManager cookieSyncMngr = CookieSyncManager.createInstance(Home.this);
            cookieSyncMngr.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            cookieSyncMngr.stopSync();
            cookieSyncMngr.sync();
        }
        String cookies = CookieManager.getInstance().getCookie(currentUrl);
        LogUtil.loge("cookies after clear: "+cookies);
    }

    private void showNoInternet(boolean offline) {
        if(Settings.OPEN_LOCAL_HTML_BY_DEFAULT){
            return;
        }

        if(offline){
            if(Settings.OPEN_LOCAL_HTML_WHEN_OFFLINE){
                if(showingLocalWebsite){
                    // already showing local website
                    return;
                }
                setOfflineHomeUrl();
                loadWebPage();
            }
            else{
                noInternet.setVisibility(View.VISIBLE);
            }
        }
        else{
            noInternet.setVisibility(View.GONE);
            if(showingLocalWebsite){
                // if previously showing local website now load online website
                setOnlineHomeUrl();
                loadWebPage();
            }
        }
    }

    public void showProgress(){
        if(Settings.LOADING_SIGN)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                pullToRefresh.setRefreshing(true);
            }
    }

    public void hideProgress(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pullToRefresh.setRefreshing(false);
        }
    }

    @Override
    public void onBackPressed() {
        if(mWebviewPop != null) {
            if(mWebviewPop.canGoBack()){
                mWebviewPop.goBack();
                return;
            }
            closeWebviewPopup();
            return;
        }

        if(webView.canGoBack()){
            webviewGoBack();
            return;
        }

        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(CloseHome);
        timerUtil.stopTimer();
        if(!Settings.CACHE_ENABLED)
            webView.destroy();
    }

    private BroadcastReceiver CloseHome = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            finish();
        }
    };

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        AdUtil.loadBannerAd(Home.this, adParent);
    }

    private void checkAndLaunchBioMetric() {
        LogUtil.loge("@@@ checkAndLaunchBioMetric....");

        try {
            boolean IsBiometricAvailable = false;

            // creating a variable for our BiometricManager
            // and lets check if our user can use biometric sensor or not
            BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
            switch (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG  | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {

                // this means we can use biometric sensor
                case BiometricManager.BIOMETRIC_SUCCESS:
                    IsBiometricAvailable = true;
                    break;

                // this means that the device doesn't have fingerprint sensor
                case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                    break;

                // this means that biometric sensor is not available
                case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                    break;

                // this means that the device doesn't contain your fingerprint
                case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                    break;
            }

            // creating a variable for our Executor
            Executor executor = ContextCompat.getMainExecutor(this);
            // this will give us result of AUTHENTICATION
            final BiometricPrompt biometricPrompt = new BiometricPrompt(Home.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                }

                // THIS METHOD IS CALLED WHEN AUTHENTICATION IS SUCCESS
                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);

                    String userName = PrefsUtil.getUserName(Home.this);
                    String password = PrefsUtil.getPassword(Home.this);

                    LogUtil.loge("@@@ onAuthenticationSucceeded..." + userName + "..." + password);

                    webView.loadUrl("javascript:(function(){document.getElementsByName('user_name')[0].value='"+ userName +"';document.getElementsByName('user_password')[0].value='"+ PrefsUtil.getPassword(Home.this) +"';})()");
                    webView.loadUrl("javascript:(function(){document.getElementById('siri_login').click();})()");
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                }
            });

            // creating a variable for our promptInfo
            // BIOMETRIC DIALOG
            final BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                    .setTitle(Home.this.getApplicationContext().getString(R.string.app_name))
                    .setDescription("Use your fingerprint to login")
                    .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG | BiometricManager.Authenticators.BIOMETRIC_WEAK | BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                    .build();

            if(IsBiometricAvailable) {
                biometricPrompt.authenticate(promptInfo);
            }

        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private void startSMSRetrieverClient() {

        try {
            AppSignatureHelper appSignatureHelper = new AppSignatureHelper(Home.this);
            ArrayList<String> appSignatures = appSignatureHelper.getAppSignatures();

            for (String hashCode : appSignatures) {
                System.out.println("@@@ appSignature..." + hashCode);
            }

            // Get an instance of SmsRetrieverClient, used to start listening for a matching SMS message.
            SmsRetrieverClient client = SmsRetriever.getClient(this /* context */);

            // Starts SmsRetriever, which waits for ONE matching SMS message until timeout
            // (5 minutes). The matching SMS message will be sent via a Broadcast Intent with
            // action SmsRetriever#SMS_RETRIEVED_ACTION.
            Task<Void> task = client.startSmsRetriever();

            // Listen for success/failure of the start Task. If in a background thread, this
            // can be made blocking using Tasks.await(task, [timeout]);
            task.addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    // Successfully started retriever, expect broadcast intent
                    System.out.println("@@@ SmsRetrieverClient Success...");
                }
            });

            task.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    // Failed to start retriever, inspect Exception for more details
                    System.out.println("@@@ SmsRetrieverClient Fail...");
                }
            });

            if (mySMSBroadcastReceiver != null) {
                Home.this.unregisterReceiver(mySMSBroadcastReceiver);
            }

            mySMSBroadcastReceiver = new MySMSBroadcastReceiver();

            Home.this.registerReceiver(mySMSBroadcastReceiver, new IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION));

            mySMSBroadcastReceiver.init(new MySMSBroadcastReceiver.OTPReceiveListener() {
                @Override
                public void onOTPReceived(String otp) {
                    // OTP Received
                    System.out.println("@@@ onOTPReceived..." + otp);

                    webView.loadUrl("javascript:(function(){document.getElementsByName('otp')[0].value='"+otp+"';})()");

                }

                @Override
                public void onOTPTimeOut() {
                    System.out.println("@@@ onOTPTimeOut...");
                }
            });

        } catch (Exception e){
            e.printStackTrace();
        }
    }
}