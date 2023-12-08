package com.medquestdiagnostics.siri;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_MEDIA_IMAGES;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import com.medquestdiagnostics.siri.util.GpsUtils;
import com.medquestdiagnostics.siri.util.Utils;

public class SplashActivity extends Activity {
    public int REQUEST_ID_MULTIPLE_PERMISSIONS = 101;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        super.onCreate(savedInstanceState);
        Utils.disableScreenCapture(SplashActivity.this);
        setContentView(R.layout.activity_splash);

        if (checkAndRequestAppLaunchPermissions()) {
            StartRecording();
        }

   }

    private void StartRecording() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
//                Intent i = new Intent(SplashActivity.this, MainActivity.class);
//                startActivity(i);
//                finish();
                try {

//                    WhistleDrivePreferences whistleDrivePreferences = new WhistleDrivePreferences(SplashActivity.this);
//                    String verificationStatus = whistleDrivePreferences.getVerificationStatus();
//                    String login = whistleDrivePreferences.getLoginStatus();

//                    if (login.equalsIgnoreCase("true")) {
//                        if (WhistleDrivePreferences.getSosStatus() == "false") {
//                            Intent in = new Intent(SplashActivity.this, MainActivity.class);
//                            in.putExtra("tabPosition", 3);
//                            startActivity(in);
//                            finish();
//                        } else {
//                            Intent in = new Intent(SplashActivity.this, MainActivity.class);
//                            in.putExtra("tabPosition", 1);
//                            startActivity(in);
//                            finish();
//                        }
//                    } else {

                    Intent i = new Intent(SplashActivity.this, Home.class);
                    startActivity(i);
                    finish();
                    //  }

                } catch (Exception e) {
                    e.printStackTrace();
                    Intent i = new Intent(SplashActivity.this, Home.class);
                    startActivity(i);
                    finish();
                }

            }
        }, 1000);
    }

    /**
     * Permissions required at app launch
     *
     * @return status
     */
    public boolean checkAndRequestAppLaunchPermissions() {

        int locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int notificationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS);
        int cameraPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA);
        int recordAudioPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int biometricPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.USE_BIOMETRIC);

        List<String> listPermissionsNeeded = new ArrayList<>();

        if (locationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (notificationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.POST_NOTIFICATIONS);
        }

        if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.CAMERA);
        }

        if (recordAudioPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.RECORD_AUDIO);
        }

        if (biometricPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.USE_BIOMETRIC);
        }

        try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                // Do something for android 13 and above versions
                int readMediaImagesPermission = ContextCompat.checkSelfPermission(this, READ_MEDIA_IMAGES);
                if (readMediaImagesPermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(READ_MEDIA_IMAGES);
                }

            } else {
                // do something for phones running an SDK before android 13
                int externalStoragePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
                if (externalStoragePermission != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(WRITE_EXTERNAL_STORAGE);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded
                            .toArray(new String[listPermissionsNeeded.size()]),
                    REQUEST_ID_MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_ID_MULTIPLE_PERMISSIONS) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted. Continue the action or workflow in your app.
                StartRecording();
            } else {
                // Explain to the user that the feature is unavailable because
                // the feature requires a permission that the user has denied.
                // At the same time, respect the user's decision. Don't link to
                // system settings in an effort to convince the user to change
                // their decision.
                StartRecording();
            }
        }
    }

}