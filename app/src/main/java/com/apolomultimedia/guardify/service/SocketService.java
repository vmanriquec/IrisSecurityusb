package com.apolomultimedia.guardify.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apolomultimedia.guardify.app.GuardifyApplication;
import com.apolomultimedia.guardify.database.ContactDB;
import com.apolomultimedia.guardify.preference.BluePrefs;
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

    @Override
    public void onCreate() {
        super.onCreate();

        Log.i(TAG, "onCreate SocketService...");

        userPrefs = new UserPrefs(this);
        contactDB = new ContactDB(this);

        GuardifyApplication app = (GuardifyApplication) getApplication();
        mSocket = app.getSocket();

    }

    Emitter.Listener lConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "lConnect");
        }
    };

    Emitter.Listener lDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "lDisconnect");
        }
    };

    Emitter.Listener lReconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "lReconnect");
        }
    };

    Emitter.Listener lSocketId = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "lSocketid");
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
            mSocket.off(Constantes.EVENT_SOCKETID);
        }
        userPrefs.setKeySocketConnected(false);
    }

    public void EmitData(String event, JSONObject data) {
        if (mSocket != null && mSocket.connected()) {
            mSocket.emit(event, data);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand SocketService");
        connectSocket();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnectSocket();
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
