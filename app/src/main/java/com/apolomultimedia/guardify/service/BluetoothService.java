package com.apolomultimedia.guardify.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apolomultimedia.guardify.model.BluetoothDeviceModel;
import com.apolomultimedia.guardify.preference.BluePrefs;
import com.apolomultimedia.guardify.preference.UserPrefs;
import com.apolomultimedia.guardify.util.Constantes;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;

public class BluetoothService extends Service {

    private String TAG = getClass().getSimpleName();

    UserPrefs userPrefs;
    BluePrefs bluePrefs;
    Handler handler;

    BluetoothAdapter bluetoothAdapter;
    BluetoothConnector.BluetoothSocketWrapper bluetoothSocket;
    BluetoothConnector bluetoothConnector;

    private boolean RUNN_THREAD = false;
    private boolean secure = true;

    @Override
    public void onCreate() {
        super.onCreate();

        userPrefs = new UserPrefs(this);
        bluePrefs = new BluePrefs(this);

        connectReceiver();

        if (!bluePrefs.getKeyPairedMaccAddress().equals("")) {
            connectDevice(bluePrefs.getKeyPairedMaccAddress());
        }

        handler = new Handler();
        handler.postDelayed(loopRequetConnect, 2000);

    }

    Runnable loopRequetConnect = new Runnable() {
        @Override
        public void run() {

            if (!bluePrefs.getKeyPairedMaccAddress().equals("")) {
                connectDevice(bluePrefs.getKeyPairedMaccAddress());
            }

            handler.removeCallbacks(this);
            handler.postDelayed(this, 5000);
        }
    };

    private void connectDevice(String mac) {

        try {
            if (userPrefs.getKeyLogged() && !mac.equals("") &&
                    (bluetoothSocket == null || !bluetoothSocket.isConnected())) {
                BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
                new ThreadConnectDevice(bluetoothDevice).start();

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void connectReceiver() {
        if (userPrefs.getKeyLogged()) {
            bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

            IntentFilter IF = new IntentFilter();
            IF.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
            IF.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            IF.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

            registerReceiver(BR_BLUETOOTH, IF);
        }
    }

    class ThreadConnectDevice extends Thread {
        BluetoothDevice bluetoothDevice;

        private ThreadConnectDevice(BluetoothDevice bluetoothDevice) {
            this.bluetoothDevice = bluetoothDevice;
        }

        @Override
        public void run() {
            boolean success = false;

            bluetoothConnector = new BluetoothConnector(this.bluetoothDevice, secure,
                    bluetoothAdapter, null);
            try {
                bluetoothSocket = bluetoothConnector.connect();
                success = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (success) {
                Log.i(TAG, "CONECTADO CORRECTAMENTE");
                bluePrefs.setKeyBlueConnected(true);
                sendInternalBroadcast(Constantes.BR_DEVICE_CONNECTED);
                bluePrefs.setKeyPairedMaccAddress(bluetoothDevice.getAddress());
                RUNN_THREAD = true;
                new ThreadCommunicationDevice().execute();
            }

        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    Handler hBluetooth = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Log.i(TAG, "handleMessage");

            byte[] writeBuf = (byte[]) msg.obj;
            int begin = (int) msg.arg1;
            int end = (int) msg.arg2;

            Log.i(TAG, "msg: " + msg);
            Log.i(TAG, "msg.what: " + msg.what);

            switch (msg.what) {
                case 1:
                    String writeMessage = new String(writeBuf);
                    writeMessage = writeMessage.substring(begin, end);
                    break;
            }

        }
    };

    class ThreadCommunicationDevice extends AsyncTask<Void, Void, Void> {
        private InputStream inputStream = null;
        private OutputStream outputStream = null;

        public ThreadCommunicationDevice() {
            try {
                inputStream = bluetoothSocket.getInputStream();
                outputStream = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {

            byte[] buffer = new byte[1024];
            int begin = 0;
            int bytes = 0;

            while (RUNN_THREAD) {

                bluePrefs.setKeyBlueConnected(true);
                sendInternalBroadcast(Constantes.BR_DEVICE_CONNECTED);

                try {

                    bytes += inputStream.read(buffer, bytes, buffer.length - bytes);

                    Log.i(TAG, new String(buffer, 0, bytes));

                    for (int i = begin; i < bytes; i++) {
                        if (buffer[i] == "#".getBytes()[0]) {
                            hBluetooth.obtainMessage(1, begin, i, buffer).sendToTarget();
                            begin = i + 1;
                            if (i == bytes - 1) {
                                bytes = 0;
                                begin = 0;
                            }
                        }
                    }


                } catch (IOException e) {
                    e.printStackTrace();
                    try {
                        bluetoothSocket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    bluePrefs.setKeyBlueConnected(false);
                    sendInternalBroadcast(Constantes.BR_DEVICE_DISCONNECTED);
                    RUNN_THREAD = false;
                    Log.i(TAG, "connection lose");
                }
            }

            return null;
        }

        public void write(byte[] buffer) {
            try {
                outputStream.write(buffer);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void cancel() {
            try {
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "onStartCommand");

        if (intent != null) {
            String mac = intent.getStringExtra("mac");
            if (mac != null) {
                connectDevice(mac);
            }
        }

        return Service.START_STICKY;
    }

    BroadcastReceiver BR_BLUETOOTH = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            Log.i(TAG, "action: " + action);

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        Log.i(TAG, "change to disabled");
                        RUNN_THREAD = false;
                        break;

                    case BluetoothAdapter.STATE_ON:
                        Log.i(TAG, "change to enabled");
                        onCreate();
                        break;
                }

            }

        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
        unregisterReceiver(BR_BLUETOOTH);
        handler.removeCallbacks(loopRequetConnect);
    }

    public void sendInternalBroadcast(String action) {
        Intent i = new Intent();
        i.setAction(action);
        sendBroadcast(i);
    }

}
