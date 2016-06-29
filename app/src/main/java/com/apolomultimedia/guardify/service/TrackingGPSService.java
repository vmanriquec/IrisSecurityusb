package com.apolomultimedia.guardify.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Main;

import org.json.JSONObject;


public class TrackingGPSService extends Service {

    private String TAG = getClass().getSimpleName();

    Handler handler;
    UserPrefs userPrefs;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        userPrefs = new UserPrefs(this);

    }

    Runnable runSendCoords = new Runnable() {
        @Override
        public void run() {

            Log.i(TAG, "runSendCoords");

            String battery = String.valueOf(Main.getBatteryLevel(TrackingGPSService.this));
            userPrefs.setKeyBatery(battery);

            if (SocketService.mSocket != null && userPrefs.getKeySocketConnected()) {
                JSONObject json = new JSONObject();
                try {
                    json.put("latitude", userPrefs.getKeyLatitud());
                    json.put("longitude", userPrefs.getKeyLongitud());
                    json.put("speed", userPrefs.getKeySpeed());
                    json.put("user_id", userPrefs.getKeyIdUsuario());
                    json.put("batery", userPrefs.getKeyBatery());
                    SocketService.EmitData("save_track", json);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            handler.removeCallbacks(this);
            handler.postDelayed(this, 10000);
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        handler.postDelayed(runSendCoords, 5000);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runSendCoords);
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

