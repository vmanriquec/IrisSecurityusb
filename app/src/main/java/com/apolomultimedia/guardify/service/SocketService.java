package com.apolomultimedia.guardify.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apolomultimedia.guardify.app.GuardifyApplication;
import com.apolomultimedia.guardify.database.ContactDB;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Constantes;
import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONObject;

public class SocketService extends Service {

    private String TAG = getClass().getSimpleName();
    public static Socket mSocket;

    UserPrefs userPrefs;
    ContactDB contactDB;
    Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "Creando el Servicio de Sockets...");

        userPrefs = new UserPrefs(this);
        contactDB = new ContactDB(this);
        handler = new Handler();

        GuardifyApplication app = (GuardifyApplication) getApplication();
        mSocket = app.getSocket();

    }

    Runnable runCheck = new Runnable() {
        @Override
        public void run() {

            if (mSocket != null && mSocket.connected()) {
                // do something
            } else {
                connectSocket();
            }

            handler.removeCallbacks(this);
            handler.postDelayed(this, 15000);
        }
    };

    Emitter.Listener lConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "conectar socket --- lConnect");
            userPrefs.setKeySocketConnected(true);
        }
    };

    Emitter.Listener lDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "Desconectar socket --- lDisconnect");
            userPrefs.setKeySocketConnected(false);
        }
    };

    Emitter.Listener lReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "Reconectar Socket --- lReconnect");
            userPrefs.setKeySocketConnected(true);
        }
    };

    Emitter.Listener lConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "Error de conexion Socket --- lReconnect");
            userPrefs.setKeySocketConnected(false);
        }
    };

    Emitter.Listener lError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "lReconnect");
            userPrefs.setKeySocketConnected(false);
        }
    };

    Emitter.Listener lSocketId = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "Identificador de socket --- lSocketid");
            JSONObject data = (JSONObject) args[0];
            try {
                String socket_id = data.getString("socket_id");
                if (socket_id != null) {
                    userPrefs.setKeySocketId(socket_id);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void connectSocket() {
        Log.i(TAG, "connecting socket");
        if (userPrefs.getKeyLogged()) {
            mSocket.on(Socket.EVENT_CONNECT, lConnect);
            mSocket.on(Socket.EVENT_DISCONNECT, lDisconnect);
            mSocket.on(Socket.EVENT_RECONNECT, lReconnect);
            mSocket.on(Socket.EVENT_CONNECT_ERROR, lConnectError);
            mSocket.on(Socket.EVENT_ERROR, lError);
            mSocket.on(Constantes.EVENT_SOCKETID, lSocketId);
            mSocket.connect();
        }
    }

    public void disconnectSocket() {
        Log.i(TAG, "disconnectSocket");
        if (mSocket != null && mSocket.connected()) {
            mSocket.disconnect();
            mSocket.off(Socket.EVENT_CONNECT);
            mSocket.off(Socket.EVENT_DISCONNECT);
            mSocket.off(Socket.EVENT_RECONNECT);
            mSocket.off(Socket.EVENT_CONNECT_ERROR);
            mSocket.off(Socket.EVENT_ERROR);
            mSocket.off(Constantes.EVENT_SOCKETID);
        }
        userPrefs.setKeySocketConnected(false);
    }

    public static void EmitData(String action, JSONObject data) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit(action, data);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand SocketService");
        connectSocket();
        handler.postDelayed(runCheck, 20000);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onStop");
        disconnectSocket();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
