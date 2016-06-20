package com.apolomultimedia.irissecurity;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.apolomultimedia.irissecurity.util.AlertDialogs;
import com.apolomultimedia.irissecurity.util.Main;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SplashActivity extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        handler = new Handler();

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

                if (Main.hasGPSEnabled(SplashActivity.this)) {

                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

                } else {
                    AlertDialogs.buildAlertNoGPS(SplashActivity.this);

                }

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
