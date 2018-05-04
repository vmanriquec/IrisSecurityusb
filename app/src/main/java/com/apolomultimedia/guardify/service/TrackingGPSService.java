package com.apolomultimedia.guardify.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Main;

import org.json.JSONObject;


public class TrackingGPSService extends Service {

    private String TAG = getClass().getSimpleName();

    Handler handler;
    UserPrefs userPrefs;
    BluePrefs bluePrefs;

    @Override
    public void onCreate() {
        super.onCreate();

        handler = new Handler();
        userPrefs = new UserPrefs(this);
        bluePrefs = new BluePrefs(this);

    }

    Runnable runSendCoords = new Runnable() {
        @Override
        public void run() {
            String battery = String.valueOf(Main.getBatteryLevel(TrackingGPSService.this));
            Log.i(TAG, "enviando coordenadas de latitud y longitud" );


            userPrefs.setKeyBatery(battery);
/*envia informacion de latitud y longitud a travez de socket parserando a json*/
            if (SocketService.mSocket != null && userPrefs.getKeySocketConnected()) {
                JSONObject json = new JSONObject();
                try {
                    json.put("latitude", userPrefs.getKeyLatitud());
                    json.put("longitude", userPrefs.getKeyLongitud());
                    json.put("speed", userPrefs.getKeySpeed());
                    json.put("user_id", userPrefs.getKeyIdUsuario());
                    json.put("suboption", bluePrefs.getKeySuboption());
                    json.put("batery", userPrefs.getKeyBatery());

                    SocketService.EmitData("save_track", json);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            handler.removeCallbacks(this);
            /*espera 10 segundos para volver a enviar longitud y latitud al servidor*/
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

