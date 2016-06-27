package com.apolomultimedia.guardify;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.AlertDialogs;
import com.apolomultimedia.guardify.util.Main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Locale;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {

    Handler handler;
    UserPrefs userPrefs;

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

            if (Main.hasConnecion(SplashActivity.this)) {

                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                if (userPrefs.getKeyLogged()) {
                    intent.putExtra("load", "normal");
                    intent = new Intent(SplashActivity.this, MainActivity.class);
                }

                startActivity(intent);
                finish();
                overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

            } else {
                AlertDialogs.buildAlertConexionRevisar(SplashActivity.this);

            }

        }
    };

    @Override
    protected void onPause() {
        super.onPause();

        handler.removeCallbacks(loopSplash);

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(base));
    }

}
