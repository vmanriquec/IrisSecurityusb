package com.apolomultimedia.guardify;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.AlertDialogs;
import com.apolomultimedia.guardify.util.Main;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();

    Handler handler;
    UserPrefs userPrefs;

    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 1000;
    private String SENDER_ID = "213856184230";
    GoogleCloudMessaging gcm;
    String regId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        userPrefs = new UserPrefs(getApplicationContext());
        handler = new Handler();

        Log.i("Splash", Locale.getDefault().getDisplayLanguage());
    }

    @Override
    protected void onResume() {
        super.onResume();

        startHandler();

    }

    private void startHandler() {
        handler.postDelayed(loopSplash, 2500);

    }

    Runnable loopSplash = new Runnable() {
        @Override
        public void run() {

            if (checkPlayServices()) {
                if (Main.hasConnecion(SplashActivity.this)) {
                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                    try {
                        regId = getRegistrationId(getApplicationContext());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (regId.isEmpty()) {
                        registerBG();
                    } else {
                        Log.i(TAG, "regId: " + regId);
                        continueSplash();
                    }

                } else {
                    AlertDialogs.buildAlertConexionRevisar(SplashActivity.this);

                }

            }

        }
    };

    private void registerBG() {
        new AsyncTask<Object, Void, String>() {
            @Override
            protected String doInBackground(Object[] params) {
                String msg;
                try {
                    if (gcm == null) {
                        gcm = GoogleCloudMessaging.getInstance(SplashActivity.this);
                    }
                    regId = gcm.register(SENDER_ID);
                    msg = "Device registered, registration ID: " + regId;

                    storeRegistrationId(SplashActivity.this, regId);
                    continueSplash();
                    Log.i(TAG, msg);
                } catch (Exception ex) {
                    msg = "Error :" + ex.getMessage();
                    Log.e(TAG, msg);
                }
                return msg;
            }

            @Override
            protected void onPostExecute(String s) {
                super.onPostExecute(s);
            }
        }.execute(null, null, null);
    }

    private void continueSplash() {
        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
        if (userPrefs.getKeyLogged()) {
            intent.putExtra("load", "normal");
            intent = new Intent(SplashActivity.this, MainActivity.class);
        }

        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(loopSplash);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.d(TAG, "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.apply();
    }

    private SharedPreferences getGCMPreferences(Context context) {
        return getSharedPreferences(SplashActivity.class.getSimpleName(), Context.MODE_PRIVATE);
    }

    private int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.e(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    private String getRegistrationId(Context context) throws Exception {
        final SharedPreferences prefs =
                getSharedPreferences(SplashActivity.class.getSimpleName(), Context.MODE_PRIVATE);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            return "";
        }

        return registrationId;
    }

}
